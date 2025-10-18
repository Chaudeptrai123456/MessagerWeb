from qdrant_client import QdrantClient
from qdrant_client.models import Filter, FieldCondition, MatchValue
from qdrant_client.models import Distance, VectorParams, PointStruct
from sklearn.decomposition import PCA

from typing import List
import requests
from pydantic import BaseModel
import numpy as np
# Gọi LocalAI để lấy embedding
# Khởi tạo Qdrant client
client = QdrantClient(host="localhost", port=6333)
target_dim = 1024

QDRANT_MAX_DIM = 300000
# Khởi tạo collection nếu chưa có
def init_collections():
    configs = {
        "users": 1024,
        "products": 1024,
        "orders": 1024
    }
    for name, size in configs.items():
        if not client.collection_exists(name):
            client.create_collection(
                collection_name=name,
                vectors_config=VectorParams(size=size, distance=Distance.COSINE)
            )
def get_all_products_from_qdrant(limit_per_page: int = 100):
    """
    Lấy toàn bộ product từ Qdrant (bao gồm id và payload).
    Hỗ trợ tự động scroll qua tất cả các trang.
    """
    all_products = []
    scroll_offset = None

    print("📦 Đang tải toàn bộ products từ Qdrant...")

    while True:
        result, scroll_offset = client.scroll(
            collection_name="products",
            limit=limit_per_page,
            offset=scroll_offset
        )

        if not result:
            break

        for point in result:
            all_products.append({
                "qdrant_id": point.id,
                "product": point.payload
            })

        print(f"✅ Đã tải {len(all_products)} sản phẩm...")

        if scroll_offset is None:
            break

    print(f"🎉 Hoàn tất! Tổng cộng {len(all_products)} products được lấy ra.")
    return all_products

def reduce_vector_dim(vector: np.ndarray, target_dim) -> np.ndarray:
    """
    Giảm chiều vector bằng cách chia thành các đoạn đều nhau và lấy trung bình mỗi đoạn.
    Phù hợp khi chỉ có 1 vector (PCA không áp dụng được).
    """
    original_dim = len(vector)
    if original_dim <= target_dim:
        return vector  # không cần giảm
    
    block_size = original_dim // target_dim
    reduced = []
    for i in range(target_dim):
        start = i * block_size
        end = start + block_size
        reduced.append(np.mean(vector[start:end]))
    return np.array(reduced, dtype=np.float32)

def get_embedding(text: str, model: str = "arcee-ai_AFM-4.5B-Q4_K_M.gguf", target_dim: int = 1024) -> List[float]:
    url = "http://localhost:8080/embeddings"
    payload = {
        "input": text,
        "model": model
    }
    try:
        response = requests.post(url, json=payload)
        response.raise_for_status()
        data = response.json()
        
        embedding = np.array(data["data"][0]["embedding"], dtype=np.float32)
        original_dim = len(embedding)
        print(f"📏 Embedding dimension từ model: {original_dim}")
        # ✅ Nếu embedding quá lớn, giảm chiều bằng PCA
        if original_dim > 2000:
            print(f"⚙️ Giảm chiều từ {original_dim} → {target_dim} bằng trung bình khối...")
            embedding = reduce_vector_dim(embedding, target_dim)

        return embedding.tolist()

    except requests.exceptions.HTTPError as http_err:
        print(f"❌ HTTP error: {http_err}")
        print(f"📦 Response: {response.text}")
        raise
    except Exception as err:
        print(f"❌ Unexpected error: {err}")
        raise
# Chuyển product thành chuỗi mô tả
def stringify_product(p: dict) -> str:
    features = ", ".join([f"{f['name']}: {f['value']}" for f in p.get("features", [])])
    discount = f"{p.get('currentDiscountPercentage', 0)*100:.0f}%"
    return f"{p['name']} - {p['category']['name']} - {p['price']} USD - {features} - Discount: {discount}. {p['description']}"
# Chuyển order thành chuỗi mô tả
def stringify_order(order: dict) -> str:
    item_descriptions = []
    for item in order["items"]:
        product = item["product"]
        features = ", ".join([f"{f['name']}: {f['value']}" for f in product.get("features", [])])
        category = product["category"]["name"] if isinstance(product["category"], dict) else product["category"]
        item_descriptions.append(
            f"{product['name']} x{item['quantity']} - {category} - {features} - {product['currentPrice']} USD"
        )
    items_text = " | ".join(item_descriptions)
    return f"Order by {order['customerName']} ({order['customerEmail']}) - {order['address']} - Total: {order['totalAmount']} USD - Status: {order['status']} - Items: {items_text}"
# Lưu product vào Qdrant
def save_product(product: dict):
    text = stringify_product(product)
    vector = get_embedding(text)
    client.upsert(
        collection_name="products",
        points=[
            PointStruct(
                id=abs(hash(product["id"])) % (10**8),
                vector=vector,
                payload=product
            )
        ]
    )
    check_product_saved(abs(hash(product["id"])) % (10**8))  # ID mà in ra ở trên

# Lưu order vào Qdrant
def save_order(order: dict):
    text = stringify_order(order)
    vector = get_embedding(text)
    client.upsert(
        collection_name="orders",
        points=[
            PointStruct(
                id=abs(hash(order["id"])) % (10**8),
                vector=vector,
                payload=order
            )
        ]
    )
def check_product_saved(qdrant_id: int):
    result = client.retrieve(
        collection_name="products",
        ids=[qdrant_id],
        with_payload=True,
        with_vectors=False
    )
    result1 = client.retrieve(
    collection_name="products",
    ids=[74847342],
    with_payload=True,
    with_vectors=True
    )
    print("check existed product "  )
    print(result)
    print("test vector")
    print(result1)
def find_similar_products(query_text:str,limit: int = 5) :
    query_vector = get_embedding(query_text)
    
    # 2️⃣ Giảm chiều nếu cần (nếu Châu đang có bước PCA/mean pooling 128000→1024)
    query_vector = reduce_vector_dim(query_vector,1024)  # nếu có hàm giảm chiều của Châu
    
    # 3️⃣ Gọi Qdrant search
    results = client.search(
        collection_name="products",
        query_vector=query_vector,
        limit=limit,
        with_payload=True
    )
    
    # 4️⃣ In ra kết quả
    print(f"\n🔍 Kết quả tìm kiếm cho: '{query_text}'")
    for r in results:
        print(f"🆔 {r.id} | 📈 Score: {r.score:.4f}")
        print(f"📦 Tên sản phẩm: {r.payload.get('name')}")
        print(f"💬 Mô tả: {r.payload.get('description')}\n")
    
    return results
def get_all_product_vectors_from_qdrant(limit_per_page: int = 100) -> List[List[float]]:
    """
    Lấy toàn bộ vector từ collection 'products' trong Qdrant.
    Tự động scroll qua các trang để tránh lỗi bộ nhớ.
    """
    all_vectors = []
    scroll_offset = None

    print("📦 Đang tải toàn bộ vectors từ Qdrant...")

    while True:
        response = client.scroll(
            collection_name="products",
            limit=limit_per_page,
            offset=scroll_offset,
            with_vector=True,
            with_payload=False
        )

        points = response.points
        if not points:
            break

        for point in points:
            all_vectors.append(point.vector)

        print(f"✅ Đã tải {len(all_vectors)} vectors...")

        scroll_offset = response.next_page_offset
        if scroll_offset is None:
            break

    print(f"🎉 Hoàn tất! Tổng cộng {len(all_vectors)} vectors được lấy ra.")
    return all_vectors

# Truy xuất product theo ID
def get_product_by_id(product_id: str) -> dict:
    result = client.scroll(
        collection_name="products",
        scroll_filter=Filter(
            must=[FieldCondition(key="id", match=MatchValue(value=product_id))]
        ),
        limit=1
    )[0]
    return result[0].payload if result else {}

# Truy xuất order theo ID
def get_order_by_id(order_id: str) -> dict:
    result = client.scroll(
        collection_name="orders",
        scroll_filter=Filter(
            must=[FieldCondition(key="id", match=MatchValue(value=order_id))]
        ),
        limit=1
    )[0]
    return result[0].payload if result else {}
# Hàm tạo embedding giả lập (nếu cần test nhanh)
def fake_embedding(seed: int) -> List[float]:
    np.random.seed(seed)
    return np.random.rand(1024).tolist()
# 📦 Model dữ liệu đầu vào cho sản phẩm (nếu dùng FastAPI)
def search_similar_products(query_text: str, top_k: int = 5):
    """
    Tìm kiếm sản phẩm gần giống (semantic search) dựa trên nội dung query_text.
    Dùng LocalAI để sinh embedding cho query, sau đó tìm trong Qdrant.
    """
    print(f"🔍 Đang tìm sản phẩm tương tự với: '{query_text}'")
    # Lấy embedding từ LocalAI
    query_vector = get_embedding(query_text)
    try:
        # Truy vấn Qdrant
        response = client.query_points(
            collection_name="products",
            query=query_vector,
            limit=top_k,
            with_payload=True,
            with_vectors=False
        )
        results = []
        for point in response.points:
            product = point.payload
            score = point.score  # cosine similarity (gần 1 là giống)
            results.append({
                "id": product.get("id"),
                "name": product.get("name"),
                "score": score,
                "price": product.get("price"),
                "category": product.get("category", {}).get("name") if product.get("category") else None,
                "description": product.get("description")
            })

        print(f"✅ Tìm thấy {len(results)} sản phẩm tương tự.")
        return results

    except Exception as e:
        print(f"❌ Lỗi khi tìm kiếm tương tự: {e}")
        return []

class Image(BaseModel):
    id: int
    filename: str
    contentType: str
    url: str
class Category(BaseModel):
    id: str
    name: str
    description: str
class Feature(BaseModel):
    id: int
    name: str
    value: str
    product: str
class Discount(BaseModel):
    id: str
    percentage: float
    startDate: str
    endDate: str
    product: str
class ProductFull(BaseModel):
    id: str
    createdAt: str
    name: str
    description: str
    quantity: int
    price: float
    currentDiscountPercentage: float
    currentPrice: float
    images: List[Image]
    category: Category
    features: List[Feature]
    discounts: List[Discount]
