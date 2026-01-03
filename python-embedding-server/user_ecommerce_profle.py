from collections import defaultdict
from collections import defaultdict
from datetime import datetime
from qdrant_client.models import PointStruct
import numpy as np
from collections import defaultdict
from typing import List, Dict
from qdrant_client import QdrantClient
import uuid
import os 
from typing import List, Dict
import time
from typing import List, Dict
from qdrant_service import get_all_products_from_qdrant,get_all_orders_from_qdrant,get_embedding
VECTOR_SIZE = int(os.getenv("VECTOR_SIZE", 768))
QDRANT_HOST = os.getenv("QDRANT_HOST", "localhost")
QDRANT_HTTP_PORT = int(os.getenv("QDRANT_HTTP_PORT", 6333))
QDRANT_COLLECTION_PRODUCTS = os.getenv("QDRANT_COLLECTION_PRODUCTS", "products")
QDRANT_COLLECTION_ORDERS = os.getenv("QDRANT_COLLECTION_ORDERS", "orders")
QDRANT_COLLECTION_USERS = os.getenv("QDRANT_COLLECTION_USERS", "users")
# text-embedding-ada-002    EMBEDDING_MODEL", " all-MiniLM-L6-v2 qwen3-embedding-0.6b
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "qwen3-embedding-0.6b")
VECTOR_SIZE = int(os.getenv("VECTOR_SIZE", 768))
LOCALAI_URL = os.getenv("LOCALAI_URL", "http://localhost:8080/embeddings")
client = QdrantClient(host=QDRANT_HOST, port=QDRANT_HTTP_PORT)

def group_orders_by_user(orders: List[Dict]) -> Dict[str, List[Dict]]:
    users = defaultdict(list)
    for order in orders:
        email = order.get("order").get("customerEmail")
        if not email:
            continue
        users[email].append(order)
    return users

def build_user_economic_profile(email: str, orders: list, product_lookup: dict) -> dict | None:
    if not orders:
        return None
    total_spent = 0.0
    order_count = len(orders)
    confirmed_orders = 0
    category_counter = defaultdict(int)
    product_counter = defaultdict(int)
    discount_order_count = 0
    order_dates = []
    for order in orders.get("orders", []) if "orders" in orders else orders:
        status = order.get("order").get("status", "").lower()
        if status not in ("confirmed", "completed", "success"):
            continue
        confirmed_orders += 1
        total_spent += float(order.get("order").get("totalAmount", 0))
        created_at = order.get("order").get("createdAt")
        if created_at:
            try:
                order_dates.append(datetime.fromisoformat(created_at))
            except Exception:
                pass
        order_has_discount = False
        for item in order.get("order").get("items", []):
            qty = int(item.get("quantity", 1))
            product_id = item.get("productId")
            if not product_id:
                continue

            product_counter[product_id] += qty
            product = product_lookup.get(product_id, {})

            category = product.get("category")
            if isinstance(category, dict):
                cat_name = category.get("name")
                if cat_name:
                    category_counter[cat_name] += qty
            try:
                if float(product.get("currentDiscountPercentage", 0)) > 0:
                    order_has_discount = True
            except Exception:
                pass
        if order_has_discount:
            discount_order_count += 1
    if confirmed_orders == 0:
        return None
    avg_order_value = total_spent / confirmed_orders
    repeat_rate = max(0, (confirmed_orders - 1) / order_count)
    purchase_frequency_days = None
    if len(order_dates) >= 2:
        order_dates.sort()
        deltas = [
            (order_dates[i] - order_dates[i - 1]).days
            for i in range(1, len(order_dates))
            if (order_dates[i] - order_dates[i - 1]).days > 0
        ]
        if deltas:
            purchase_frequency_days = float(np.mean(deltas))
    print("DEBUG completed profile for", email)
    return {
        "email": email,
        "order_count": order_count,
        "confirmed_order_count": confirmed_orders,
        "total_spent": round(total_spent, 2),
        "avg_order_value": round(avg_order_value, 2),
        "repeat_rate": round(repeat_rate, 3),
        "purchase_frequency_days": purchase_frequency_days,
        "discount_sensitivity": round(discount_order_count / confirmed_orders, 3),
        "category_distribution": dict(category_counter),
        "top_products": dict(sorted(product_counter.items(), key=lambda x: x[1], reverse=True)[:5]),
        "last_order_at": max(order_dates).isoformat() if order_dates else None,
        "created_at": datetime.utcnow().isoformat()
    }

def build_user_embedding(
    orders: List[dict],
    product_lookup: Dict[str, dict]  # giữ cho tương lai
) -> np.ndarray | None:
    vectors = []
    print("DEBUG building user embedding from orders:", len(orders))
    for order in orders:
        vec = order.get("vector")
        if not isinstance(vec, list):
            print("⚠️ Order has no vector:", order.get("qdrant_id"))
            continue
        arr = np.array(vec, dtype=np.float32)
        if arr.ndim != 1 or arr.size == 0:
            continue
        vectors.append(arr)
    print("DEBUG user order vectors count:", len(vectors))
    if not vectors:
        return None
    return np.mean(vectors, axis=0)

def upsert_user_profile(profile: dict, embedding: List[float]):
    print("🚀 Upserting user profile for:", profile)
    print("DEBUG embedding size:", embedding and len(embedding))
    client.upsert(
        collection_name=QDRANT_COLLECTION_USERS,
        points=[
            PointStruct(
                id=str(uuid.uuid4()),
                vector=embedding,
                payload=profile
            )
        ]
    )

def normalize_vector(vec, target_dim: int) -> np.ndarray | None:
    if vec is None:
        return None
    arr = np.array(vec, dtype=np.float32)
    if arr.ndim != 1 or arr.size == 0:
        return None
    if len(arr) > target_dim:
        arr = reduce_vector_dim_mean(arr, target_dim)
    elif len(arr) < target_dim:
        pad = np.zeros(target_dim - len(arr), dtype=np.float32)
        arr = np.concatenate([arr, pad])
    return arr

def build_all_user_ecommerce_profiles():
    print("🚀 Building user ecommerce profiles...")
    products = get_all_products_from_qdrant()
    orders = get_all_orders_from_qdrant()
    
    product_lookup = build_product_lookup(products)
    users = group_orders_by_user(orders)
    for email, user_orders in users.items():
        profile = build_user_economic_profile(email, user_orders, product_lookup)
        print("DEBUG profile for", email, ":", profile)
        if not profile:
            continue
        behavior_embedding = build_user_embedding(user_orders, product_lookup)
        semantic_embedding = get_embedding(stringify_user_profile(profile))
        vectors = []
        behavior_vec = normalize_vector(behavior_embedding, VECTOR_SIZE)
        semantic_vec = normalize_vector(semantic_embedding, VECTOR_SIZE)
        if behavior_vec is not None:
            vectors.append(behavior_vec)
        if semantic_vec is not None:
            vectors.append(semantic_vec)
        if not vectors:
            print(f"⚠️ Skip user {email}: no valid vectors")
            continue
        final_embedding = np.mean(np.stack(vectors), axis=0).tolist()
        upsert_user_profile(profile, final_embedding)
    print("✅ User ecommerce profiles completed")

def stringify_user_profile(profile: dict) -> str:
    return (
        f"User ecommerce profile. "
        f"Total orders: {profile.get('order_count')}, "
        f"Confirmed orders: {profile.get('confirmed_order_count')}, "
        f"Repeat purchase rate: {profile.get('repeat_rate')}, "
        f"Avg days between orders: {profile.get('purchase_frequency_days')}. "
        f"Total spent: {profile.get('total_spent')} VND, "
        f"Avg order value: {profile.get('avg_order_value')} VND. "
        f"Discount sensitivity: {profile.get('discount_sensitivity')}. "
        f"Top categories: {list(profile.get('category_distribution', {}).keys())}. "
        f"Top products: {list(profile.get('top_products', {}).keys())}."
    )

def build_product_lookup(products: list) -> dict:
    lookup = {}
    for prod in products:
        if not isinstance(prod, dict):
            continue
        pid = prod.get("id")
        if pid:
            # nếu product có vector thì gắn vào payload
            if "vector" in prod:
                prod["embedding"] = prod["vector"]
            lookup[pid] = prod
    return lookup
def get_all_user_profiles_from_qdrant(
    limit_per_page: int = 10,
    max_retries: int = 3,
    retry_delay_sec: float = 0.5,
) -> List[Dict]:
    """
    Lấy toàn bộ user profiles từ Qdrant (có cả payload và vector)
    An toàn, có retry, không gây OutputTooSmall
    """
    all_users: List[Dict] = []
    scroll_offset = None

    print("🚀 Start scrolling users from Qdrant...")

    while True:
        attempt = 0
        while True:
            try:
                points, scroll_offset = client.scroll(
                    collection_name=QDRANT_COLLECTION_USERS,
                    limit=limit_per_page,
                    offset=scroll_offset,
                    with_payload=True,
                    with_vectors=True,   # 🔥 QUAN TRỌNG
                )
                print(f"DEBUG fetched {len(points)} points")
                break
            except Exception as e:
                attempt += 1
                if attempt > max_retries:
                    print(f"❌ Qdrant scroll failed: {e}")
                    return all_users
                print(f"⚠️ Retry scroll attempt {attempt} after {retry_delay_sec}s")
                time.sleep(retry_delay_sec)

        if not points:
            break

        for point in points:
            all_users.append({
                "qdrant_id": point.id,
                "email": point.payload.get("email"),
                "profile": point.payload,
                "vector_size": len(point.vector) if point.vector else 0,
                "embedding": point.vector,  # ✅ THÊM VÀO ĐÂY
            })

        if scroll_offset is None:
            break

    print(f"✅ Done. Total users fetched: {len(all_users)}")
    return all_users

 
def delete_all_users_from_qdrant(
    batch_size: int = 100,
    sleep_sec: float = 0.2,
):
    """
    Xoá toàn bộ points trong collection users (an toàn, batch nhỏ)
    """
    scroll_offset = None
    total_deleted = 0

    print("🔥 Start deleting all users from Qdrant...")

    while True:
        points, scroll_offset = client.scroll(
            collection_name=QDRANT_COLLECTION_USERS,
            limit=batch_size,
            offset=scroll_offset,
            with_payload=False,
            with_vectors=False,
        )

        if not points:
            break

        ids: List[str] = [p.id for p in points]

        client.delete(
            collection_name=QDRANT_COLLECTION_USERS,
            points_selector=ids,
        )

        total_deleted += len(ids)
        print(f"🗑️ Deleted {len(ids)} users (total: {total_deleted})")

        time.sleep(sleep_sec)  # tránh overload

        if scroll_offset is None:
            break

    print(f"✅ DONE. Total users deleted: {total_deleted}")
def show_all_user_profiles():
    """
    In ra toàn bộ user ecommerce profiles từ Qdrant
    """
    users = get_all_user_profiles_from_qdrant()
    print(f"🚀 Tổng số user profiles: {len(users)}")
    # for u in users:
    #     print("=" * 60)
    #     print(f"Qdrant ID: {u['qdrant_id']}")
    #     print(f"Email: {u['email']}")
    #     print(f"Vector size: {u['vector_size']}")
    #     print("Profile payload:")
    #     for k, v in u["profile"].items():
    #         print(f"  {k}: {v}")