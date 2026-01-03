import os
from typing import List, Dict, Optional
import numpy as np
import uuid
import requests
from pydantic import BaseModel
from qdrant_client import QdrantClient
from qdrant_client.models import (
    Distance,
    VectorParams,
    PointStruct,
    Filter,
    FieldCondition,
    MatchValue,
)
from datetime import datetime
from qdrant_client import QdrantClient
from qdrant_client.http import models
from qdrant_client import models
import time
from typing import Dict
import json
from qdrant_client.models import Filter, FieldCondition, MatchValue, Range

# ========= Config =========
QDRANT_HOST = os.getenv("QDRANT_HOST", "localhost")
QDRANT_HTTP_PORT = int(os.getenv("QDRANT_HTTP_PORT", 6333))
QDRANT_COLLECTION_PRODUCTS = os.getenv("QDRANT_COLLECTION_PRODUCTS", "products")
QDRANT_COLLECTION_ORDERS = os.getenv("QDRANT_COLLECTION_ORDERS", "orders")
QDRANT_COLLECTION_USERS = os.getenv("QDRANT_COLLECTION_USERS", "users")
# text-embedding-ada-002    EMBEDDING_MODEL", " all-MiniLM-L6-v2 qwen3-embedding-0.6b
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "qwen3-embedding-0.6b")
VECTOR_SIZE = int(os.getenv("VECTOR_SIZE", 768))
LOCALAI_URL = os.getenv("LOCALAI_URL", "http://localhost:8080/embeddings")

# ========= Client =========
client = QdrantClient(host=QDRANT_HOST, port=QDRANT_HTTP_PORT)

# ========= Schemas (nếu dùng với FastAPI) =========
class Image(BaseModel):
    id: int
    filename: str
    contentType: str
    url: str

class Category(BaseModel):
    id: str
    name: str
    description: Optional[str] = None

class Feature(BaseModel):
    id: int
    name: str
    value: str
    product: Optional[str] = None

class Discount(BaseModel):
    id: str
    percentage: float
    startDate: Optional[str] = None
    endDate: Optional[str] = None
    product: Optional[str] = None

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

class OrderItem(BaseModel):
    id: str
    quantity: int
    price: float
    product: ProductFull
    order: str

class Order(BaseModel):
    id: str
    createdAt: str
    customerName: str
    customerEmail: str
    address: str
    status: str
    totalAmount: float
    items: List[OrderItem]


# ========= Init collections =========
def init_collections():
    configs = {
        QDRANT_COLLECTION_USERS: VECTOR_SIZE,
        QDRANT_COLLECTION_PRODUCTS: VECTOR_SIZE,
        QDRANT_COLLECTION_ORDERS: VECTOR_SIZE,
    }
    for name, size in configs.items():
        if not client.collection_exists(name):
            client.create_collection(
                collection_name=name,
                vectors_config=VectorParams(size=size, distance=Distance.COSINE),
            )
        else:
            # Nếu collection đã tồn tại nhưng sai dimension thì recreate
            info = client.get_collection(name)
            if info.config.params.vectors.size != size:
                client.recreate_collection(
                    collection_name=name,
                    vectors_config=VectorParams(size=size, distance=Distance.COSINE),
                )

# ========= Embedding helpers =========

def reduce_vector_dim_mean(vector: np.ndarray, target_dim: int) -> np.ndarray:
    original_dim = len(vector)

    if original_dim == target_dim:
        return vector.astype(np.float32)

    # Nếu nhỏ hơn → PAD
    if original_dim < target_dim:
        pad = np.zeros(target_dim - original_dim, dtype=np.float32)
        return np.concatenate([vector.astype(np.float32), pad])

    # Nếu lớn hơn → RESAMPLE
    indices = np.linspace(0, original_dim, target_dim + 1, dtype=int)
    reduced = np.array([
        vector[indices[i]:indices[i+1]].mean()
        for i in range(target_dim)
    ], dtype=np.float32)

    return reduced


def get_embedding(
    text: str,
    model: Optional[str] = None,
    target_dim: Optional[int] = None
) -> List[float]:

    print("🔥 Calling get_embedding()")

    model = model or EMBEDDING_MODEL
    target_dim = target_dim or VECTOR_SIZE

    payload = {"input": text, "model": model}

    # ---- FIX: Retry để tránh lỗi 500 của LocalAI ----
    for attempt in range(3):
        try:
            print(f"➡️ POST {LOCALAI_URL} with model={model} (attempt {attempt+1})")

            response = requests.post(
                LOCALAI_URL,
                json=payload,
                timeout=60  # tránh nghẽn socket
            )
            response.raise_for_status()   # nếu 500 -> jump xuống except

            data = response.json()
            embedding = np.array(data["data"][0]["embedding"], dtype=np.float32)

            # ---- FIX: Giảm/đệm chiều vector ----
            original_dim = len(embedding)
            if original_dim != target_dim:
                if original_dim > target_dim:
                    embedding = reduce_vector_dim_mean(embedding, target_dim)
                else:
                    pad = np.zeros(target_dim - original_dim, dtype=np.float32)
                    embedding = np.concatenate([embedding, pad])

            # ---- FIX: Sleep 200ms để LocalAI không choke ----
            time.sleep(0.2)

            return embedding.tolist()

        except Exception as e:
            print(f"⚠️ LocalAI failed on embedding (attempt {attempt+1}): {e}")
            time.sleep(0.5)  # đợi LocalAI hồi lại

    # ---- Nếu fail 3 lần liên tục → lỗi thật ----
    raise RuntimeError("❌ LocalAI embedding failed after 3 retries")

def get_embedding(text: str, model: Optional[str] = None, target_dim: Optional[int] = None) -> List[float]:
    # Gọi LocalAI để lấy embedding. Tự động giảm chiều nếu dimension lớn hơn VECTOR_SIZE.
    model = model or EMBEDDING_MODEL
    target_dim = target_dim or VECTOR_SIZE

    payload = {"input": text, "model": model}
    print(LOCALAI_URL)
    response = requests.post(LOCALAI_URL, json=payload)
    response.raise_for_status()

    data = response.json()
    embedding = np.array(data["data"][0]["embedding"], dtype=np.float32)
    original_dim = len(embedding)

    if original_dim != target_dim:
        # Nếu model trả về dimension khác VECTOR_SIZE, giảm (hoặc giữ nguyên nếu nhỏ hơn)
        if original_dim > target_dim:
            embedding = reduce_vector_dim_mean(embedding, target_dim)
        else:
            # Nếu nhỏ hơn, pad zeros để khớp kích thước
            pad = np.zeros(target_dim - original_dim, dtype=np.float32)
            embedding = np.concatenate([embedding, pad])

    return embedding.tolist()


# ========= Stringify =========
def stringify_product(p: Dict) -> str:
    # xử lý features an toàn
    features_list = []
    for f in p.get("features", []):
        if isinstance(f, dict):
            name = f.get("name", "")
            value = f.get("value", "")
            features_list.append(f"{name}: {value}")
        else:
            features_list.append(str(f))
    features = ", ".join(features_list)

    # xử lý category
    category = p.get("category", "")
    if isinstance(category, dict):
        category_name = category.get("name", "")
    elif isinstance(category, (list, tuple)):
        category_name = ", ".join(str(x) for x in category)
    else:
        category_name = str(category)

    # description
    desc = p.get("description", "")
    if isinstance(desc, (list, dict)):
        desc = json.dumps(desc, ensure_ascii=False)
    else:
        desc = str(desc)

    # discount
    discount_pct = p.get("currentDiscountPercentage", 0)
    if not isinstance(discount_pct, (int, float)):
        try:
            discount_pct = float(discount_pct)
        except Exception:
            discount_pct = 0

    # price
    price = p.get("price", p.get("currentPrice", ""))
    if isinstance(price, (list, dict)):
        price = json.dumps(price, ensure_ascii=False)
    else:
        price = str(price)

    return f"{p.get('name','')} - {category_name} - {price} USD - {features} - Discount: {discount_pct}%. {desc}"
def stringify_order(order: Dict) -> str:
    item_descriptions = []
    for item in order.get("items", []):
        product = item.get("product", {})
        features = ", ".join([f"{f['name']}: {f['value']}" for f in product.get("features", [])])
        category = product["category"]["name"] if isinstance(product.get("category"), dict) else product.get("category")
        price = product.get("currentPrice", product.get("price", ""))
        item_descriptions.append(
            f"{product.get('name','')} x{item.get('quantity',1)} - {category} - {features} - {price} USD"
        )
    items_text = " | ".join(item_descriptions)
    return f"Order by {order.get('customerName','')} ({order.get('customerEmail','')}) - {order.get('address','')} - Total: {order.get('totalAmount','')} USD - Status: {order.get('status','')} - Items: {items_text}"


# ========= Upsert =========
def _stable_id(s: str) -> int:
    # Tạo id số ổn định từ chuỗi
    return abs(hash(str(s))) % (10**12)


def save_product(product: Dict):
    """
    Lưu 1 product vào Qdrant. Không xóa collection.
    """
    text = stringify_product(product)
    vector = get_embedding(text)
    client.upsert(
        collection_name=QDRANT_COLLECTION_PRODUCTS,
        points=[
            PointStruct(
                id=_stable_id(product.get("id")),
                vector=vector,
                payload=product,
            )
        ],
    )

def normalize_order(o: Dict):
    return {
        "id": o.get("id"),
        "createdAt": o.get("createdAt"),
        "customerName": o.get("customerName"),
        "customerEmail": o.get("customerEmail"),
        "address": o.get("address"),
        "status": o.get("status"),
        "totalAmount": o.get("totalAmount"),
        "items": [
            {
                "id": item.get("id"),
                "productId": item.get("product", {}).get("id") if item.get("product") else None,
                "quantity": item.get("quantity"),
                "price": item.get("price"),
            }
            for item in o.get("items", [])
        ],
        "userId": o.get("user", {}).get("id") if o.get("user") else None
    }
def save_order(order: Dict):
    clean_order = normalize_order(order)

    text = stringify_order(clean_order)
    print("DEBUG save order " + text)
    vector = get_embedding(text)

    client.upsert(
        collection_name=QDRANT_COLLECTION_ORDERS,
        points=[
            PointStruct(
                id=_stable_id(clean_order["id"]),
                vector=vector,
                payload=clean_order,
            )
        ],
    )

import numpy as np

def build_product_embedding(product: dict) -> List[float]:
    """
    - name: cao nhất
    - discount + price: nhì
    - description + features: thấp
    """
    vectors = []
    weights = []

    # name trọng số cao
    if product.get("name"):
        vectors.append(get_embedding(product["name"]))
        weights.append(4.0)

    # discount + price trọng số nhì
    if product.get("discount"):
        vectors.append(get_embedding(str(product["discount"])))
        weights.append(2.0)
    if product.get("price"):
        vectors.append(get_embedding(str(product["price"])))
        weights.append(2.0)
    # description trọng số thấp
    if product.get("description"):
        vectors.append(get_embedding(product["description"]))
        weights.append(1.0)

    # features trọng số thấp
    if product.get("features"):
        for f in product["features"]:
            vectors.append(get_embedding(f["name"] + " " + f["value"]))
            weights.append(0.8)

    # Weighted average
    weighted_vector = np.average(vectors, axis=0, weights=weights)
    return weighted_vector.tolist()
def upsert_products_batch(products: List[Dict]):
    """
    Upsert theo batch để nhanh hơn khi sync nhiều sản phẩm.
    """
    points = []
    for p in products:
        # text = stringify_product(p)
        vector = build_product_embedding(p)
        time.sleep(0.2)  # nghỉ 200ms để LocalAI thở
        points.append(PointStruct(id=_stable_id(p.get("id")), vector=vector, payload=p))
    if points:
        client.upsert(collection_name=QDRANT_COLLECTION_PRODUCTS, points=points)

def upsert_orders_bath(orders: List[Dict]):
    points = []
    for o in orders:
        text = stringify_order(o)
        vector = get_embedding(text)
        time.sleep(0.2)  # nghỉ 200ms để LocalAI thở
        points.append(PointStruct(id=_stable_id(o.get("id")), vector=vector, payload=o))
    if points:
        client.upsert(collection_name=QDRANT_COLLECTION_ORDERS, points=points)

# ========= Retrieve / Scroll / Search =========
def get_product_by_id(product_id: str) -> Dict:
    result, _ = client.scroll(
        collection_name=QDRANT_COLLECTION_PRODUCTS,
        scroll_filter=Filter(must=[FieldCondition(key="id", match=MatchValue(value=product_id))]),
        limit=1,
        with_payload=True,
        with_vectors=False,
    )
    return result[0].payload if result else {}


def get_order_by_id(order_id: str) -> Dict:
    result, _ = client.scroll(
        collection_name=QDRANT_COLLECTION_ORDERS,
        scroll_filter=Filter(must=[FieldCondition(key="id", match=MatchValue(value=order_id))]),
        limit=1,
        with_payload=True,
        with_vectors=False,
    )
    return result[0].payload if result else {}


def get_all_products_from_qdrant(limit_per_page: int = 1) -> List[Dict]:
    all_products = []
    scroll_offset = None
    while True:
        points, scroll_offset = client.scroll(
            collection_name=QDRANT_COLLECTION_PRODUCTS,
            limit=limit_per_page,
            offset=scroll_offset,
            with_payload=True,
            with_vectors=True,  
        )
        if not points:
            break
        for point in points:
            all_products.append({
                "qdrant_id": point.id,
                "product": point.payload,
                "vector": point.vector,
            })

        if scroll_offset is None:
            break
    return all_products

def get_all_orders_from_qdrant(limit_per_page: int = 100) -> List[Dict]:
    all_order= []
    scroll_offset = None
    while True:
        points, scroll_offset = client.scroll(
            collection_name=QDRANT_COLLECTION_ORDERS,
            limit=limit_per_page,
            offset=scroll_offset,
            with_payload=True,
            with_vectors=True,
        )
        if not points:
            break
        for point in points:
            all_order.append({
                "qdrant_id":point.id,
                "order": point.payload,
                "vector": point.vector ,
            })
        if scroll_offset is None:
            break
    return all_order

def get_all_product_vectors_from_qdrant(limit_per_page: int = 100) -> List[List[float]]:
    all_vectors = []
    scroll_offset = None
    while True:
        points, scroll_offset = client.scroll(
            collection_name=QDRANT_COLLECTION_PRODUCTS,
            limit=limit_per_page,
            offset=scroll_offset,
            with_vectors=False,
            with_payload=True,
        )
        if not points:
            break
        for point in points:
            all_vectors.append(point.vector)
        if scroll_offset is None:
            break
    return all_vectors


def find_similar_products(query_text: str, limit: int = 5, filters: Optional[Filter] = None):
    query_vector = get_embedding(query_text)
    results = client.search(
        collection_name=QDRANT_COLLECTION_PRODUCTS,
        query_vector=query_vector,
        limit=limit,
        with_payload=True,
        filter=filters,
    )
    return results

 
def find_similar_orders(query_text: str, limit: int = 5, filters: Optional[Filter] = None):
    query_vector = get_embedding(query_text)
    results = client.search(
        collection_name=QDRANT_COLLECTION_ORDERS,
        query_vector=query_vector,
        limit=limit,
        with_payload=True,
        filter=filters,
    )
    return results
# ========= Delete helpers =========

 
def delete_all_users():
    client.delete(
        collection_name=QDRANT_COLLECTION_USERS,
        points_selector={"filter": {}}
    )
    print(f"🗑️ Đã xóa toàn bộ users trong collection '{QDRANT_COLLECTION_USERS}'")
# ========= Delete helpers =========
def delete_all_products():
    """
    Xóa toàn bộ products trong collection.
    """
    client.delete( collection_name=QDRANT_COLLECTION_PRODUCTS, points_selector={"all": True} )
    # print(f"🗑️ Đã xóa toàn bộ products trong collection '{QDRANT_COLLECTION_PRODUCTS}'")


def delete_all_orders():
    """
    Xóa toàn bộ orders trong collection.
    """
    client.delete(
        collection_name=QDRANT_COLLECTION_ORDERS,
        points_selector={"filter": {}}
    )
    print(f"🗑️ Đã xóa toàn bộ orders trong collection '{QDRANT_COLLECTION_ORDERS}'")
def clear_and_recreate_products():
    client.delete_collection(QDRANT_COLLECTION_PRODUCTS)
    client.create_collection(
        collection_name=QDRANT_COLLECTION_PRODUCTS,
        vectors_config=VectorParams(size=VECTOR_SIZE, distance=Distance.COSINE),
    )
    print(f"🗑️ Đã xóa và tạo lại collection '{QDRANT_COLLECTION_PRODUCTS}'")

def clear_and_recreate_orders():
    client.delete_collection(QDRANT_COLLECTION_ORDERS)
    client.create_collection(
        collection_name=QDRANT_COLLECTION_ORDERS,
        vectors_config=VectorParams(size=VECTOR_SIZE, distance=Distance.COSINE),
    )
    print(f"🗑️ Đã xóa và tạo lại collection '{QDRANT_COLLECTION_ORDERS}'")

def get_product_by_id(product_id: str):
    result, _ = client.scroll(
        collection_name=QDRANT_COLLECTION_PRODUCTS,
        scroll_filter=Filter(
            must=[FieldCondition(key="id", match=MatchValue(value=product_id))]
        ),
        limit=1,
        with_payload=True,
        with_vectors=False,
    )
    return result[0].payload if result else {}

def recommend_products_for_user(email: str, limit: int = 10):
    # Lấy tất cả orders của user
    orders, _ = client.scroll(
        collection_name=QDRANT_COLLECTION_ORDERS,
        scroll_filter=Filter(
            must=[
                FieldCondition(
                    key="customerEmail",
                    match=MatchValue(value=email)
                )
            ]
        ),
        with_payload=True,
        with_vectors=True,
        limit=100
    )
    # ---- FIX 1: Lấy productId đúng key ----
    product_ids = []
    for o in orders:
        for item in o.payload.get("items", []):
            pid = item.get("productId")
            if pid:
                product_ids.append(pid)

    if not product_ids:
        print("No productIds found")
        return []

    print("Product IDs:", product_ids)
    # Lấy vector sản phẩm đã mua
    user_vectors = []
    purchased_set = set(product_ids)
    scroll_offset = None
    while True:
        points, scroll_offset = client.scroll(
            collection_name=QDRANT_COLLECTION_PRODUCTS,
            limit=100,
            offset=scroll_offset,
            with_payload=True,
            with_vectors=True,
        )
        if not points:
            break

        for p in points:
            pid = p.payload.get("id")
            if pid in purchased_set and p.vector:
                user_vectors.append(np.array(p.vector, dtype=np.float32))

        if scroll_offset is None:
            break

    if not user_vectors:
        print("No vectors found for purchased products")
        return []

    # Tạo user profile vector
    user_profile = np.mean(np.stack(user_vectors), axis=0).tolist()

    # Search sản phẩm gần với user profile
    results = client.search(
        collection_name=QDRANT_COLLECTION_PRODUCTS,
        query_vector=user_profile,
        limit=limit * 2,
        with_payload=True
    )

    products = []
    for r in results:
        p = r.payload
        pid = p.get("id")
        print(str(p))
        products.append({
            "id": pid,
            "name": p.get("name"),
            "description": p.get("category.description"),
            "price": p.get("price", p.get("currentPrice")),
            "discount": p.get("discount"),
            "description": p.get("category", {}).get("description")
                if isinstance(p.get("category"), dict) else p.get("category"),
            "images": [p.get("image")],
            "already_purchased": pid in purchased_set,
            "score": r.score
        })

        if len(products) >= limit:
            break

    return products
def search_with_description(description: str):
    # vectorize the description 
    vector_description = get_embedding(description)
    products = client.query_points(
        collection_name=QDRANT_COLLECTION_PRODUCTS,   #  
        query=vector_description,
        limit=10,
        with_vectors=False,
    )
    result = []
    for product in products.points:
        # print(str(product))
        result.append(product.payload)
    return result
def recommend_with_filters(email: str, category: str | None = None, min_price: float | None = None, max_price: float | None = None, limit: int = 10):
    # Lấy orders của user
    orders, _ = client.scroll(
        collection_name=QDRANT_COLLECTION_ORDERS,
        scroll_filter=Filter(must=[FieldCondition(key="customerEmail", match=MatchValue(value=email))]),
        with_payload=True,
        with_vectors=False,
        limit=100
    )

    product_ids = []
    for o in orders:
        for item in o.payload.get("items", []):
            pid = item.get("product", {}).get("id")
            if pid:
                product_ids.append(pid)

    if not product_ids:
        return []

    # Lấy vector các sản phẩm đã mua
    user_vectors = []
    purchased_set = set(product_ids)
    scroll_offset = None
    while True:
        points, scroll_offset = client.scroll(
            collection_name=QDRANT_COLLECTION_PRODUCTS,
            limit=100,
            offset=scroll_offset,
            with_payload=True,
            with_vectors=False,
        )
        if not points:
            break
        for p in points:
            pid = p.payload.get("id")
            if pid in purchased_set and p.vector:
                user_vectors.append(np.array(p.vector, dtype=np.float32))
        if scroll_offset is None:
            break

    if not user_vectors:
        return []

    # Tạo user profile vector
    user_profile = np.mean(np.stack(user_vectors), axis=0).tolist()

    # Tạo bộ lọc payload
    filter_conditions = []
    if category:
        filter_conditions.append(FieldCondition(key="category.name", match=MatchValue(value=category)))
    if min_price is not None or max_price is not None:
        price_range = {}
        if min_price is not None:
            price_range["gte"] = min_price
        if max_price is not None:
            price_range["lte"] = max_price
        filter_conditions.append(FieldCondition(key="price", range=Range(**price_range)))

    payload_filter = Filter(must=filter_conditions) if filter_conditions else None

    # Search sản phẩm gần với user profile
    results = client.search(
        collection_name=QDRANT_COLLECTION_PRODUCTS,
        query_vector=user_profile,
        limit=limit * 2,
        with_payload=True,
        filter=payload_filter
    )

    # Trả về kết quả, có đánh dấu đã mua
    products = []
    for r in results:
        pid = r.payload.get("id")
        p = r.payload
        products.append({
            "id": p.get("id"),
            "name": p.get("name"),
            "description": p.get("description"),
            "price": p.get("price", p.get("currentPrice")),
            "category": p.get("category", {}).get("name") if isinstance(p.get("category"), dict) else p.get("category"),
            "images": [img.get("url") for img in p.get("image", [])],
            "already_purchased": pid in purchased_set,
            "score": r.score
        })
        if len(products) >= limit:
            break

    return {"products": products}