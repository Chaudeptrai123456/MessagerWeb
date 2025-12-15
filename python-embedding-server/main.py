# uvicorn main:app --reload
from fastapi import FastAPI,Body
from qdrant_service import clear_and_recreate_orders,clear_and_recreate_products,search_with_description,recommend_products_for_user, clear_and_recreate_products,clear_and_recreate_orders,init_collections, save_product, save_order,get_all_orders_from_qdrant,get_all_products_from_qdrant,stringify_product,get_embedding,find_similar_products,delete_all_users,delete_all_products,delete_all_orders
from qdrant_client.models import Filter, FieldCondition, MatchValue
from qdrant_client import QdrantClient
from pydantic import BaseModel
from typing import List, Optional
from fastapi import HTTPException
from sync_service import sync_products_to_qdrant,sync_orders_to_qdrant
from apscheduler.schedulers.background import BackgroundScheduler
import atexit
app = FastAPI(title="AI Recommendation Service")
client = QdrantClient(host="localhost", port=6333)
init_collections()
app = FastAPI()

class SearchRequest(BaseModel):
    description: str

@app.post("/search")
def handle_search(req: SearchRequest):
    result =  search_with_description(req.description)
    return {"result": result}

@app.get("/recomments")
def handle_recomment_product():
    email = "phamchaugiatu123@gmail.com"
    result = recommend_products_for_user(email)
    return {"products": result}

def auto_sync():
    try:
        print("🔄 Auto sync started...")
        sync_products_to_qdrant()
        sync_orders_to_qdrant()
        print("✅ Auto sync completed at 00:00")
    except Exception as e:
        print("❌ Auto sync error:", e)

# Scheduler chạy lúc 00:00 mỗi ngày
scheduler = BackgroundScheduler()
scheduler.add_job(auto_sync, 'cron', hour=0, minute=0)
scheduler.start()

# Tắt scheduler khi app shutdown
atexit.register(lambda: scheduler.shutdown())
@app.get("/sync_postgres_qdrant")
def sync_postgres_qdrant():
    try:
        sync_products_to_qdrant()
        sync_orders_to_qdrant()
        return {"status": "recreated collections and synced"}
    except Exception as e:
        print("❌ Sync error:", e)
        raise HTTPException(status_code=500, detail=str(e))
@app.post("/add_product")
def add_product(data: dict):
    try:
        print("📦 Nhận data:", data)
        save_product(data)
        return {"status": "Product added"}
    except Exception as e:
        print("❌ Lỗi khi xử lý sản phẩm:", e)
        raise HTTPException(status_code=500, detail=str(e))
@app.post("/add_order")
def add_order(data: dict):
    save_order(data)
    return {"status": "Order added"}
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
@app.get("/product/{product_id}")
def read_product(product_id: str):
    product = get_product_by_id(product_id)
    if not product:
        return {"error": "Product not found"}
    return product
@app.get("/order/{order_id}")
def read_order(order_id: str):
    order = get_order_by_id(order_id)
    if not order:
        return {"error": "Order not found"}
    return order
@app.get("/orders")
def get_all_orders():
    try:
        order = get_all_orders_from_qdrant()
        return order
    except Exception as e :
        print("Lỗi khi lấy danh sách orders: ",e)
        raise HTTPException(status_code=500, detail=str(e))
@app.get("/products")
def get_all_products():
    try:
        products = get_all_products_from_qdrant()
        return products
    except Exception as e:
        print("❌ Lỗi khi lấy danh sách sản phẩm:", e)
        raise HTTPException(status_code=500, detail=str(e))
class SimilarProductRequest(BaseModel):
    text: str
    limit: Optional[int] = 5
@app.post("/find_similar_products")
def find_similar_product(body: dict = Body(...)):
    try:
        # Lấy text từ body JSON
        query_text = body.get("text", "")
        limit = body.get("limit", 7)
        if not query_text:
            return {"error": "Thiếu trường 'text' trong body JSON"}
        results = find_similar_products(query_text, limit)
        return results
    except Exception as e:
        print(f"❌ Lỗi khi tìm kiếm tương tự: {e}")
        return {"error": str(e)}