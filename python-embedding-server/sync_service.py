import os
import psycopg2
from psycopg2.extras import RealDictCursor
from qdrant_service import (
    upsert_products_batch,
    upsert_orders_bath,
    save_order,
    client,
    QDRANT_COLLECTION_PRODUCTS,
    QDRANT_COLLECTION_ORDERS,
)
from typing import List, Dict
# ========= Load từ Postgres =========
def load_products_from_postgres() -> List[Dict]:
    conn = psycopg2.connect(
        dbname=os.getenv("POSTGRES_DB", "ecommerce"),
        user=os.getenv("POSTGRES_USER", "master"),
        password=os.getenv("POSTGRES_PASSWORD", "123"),
        host=os.getenv("POSTGRES_HOST", "localhost"),
        port=int(os.getenv("POSTGRES_PORT", 5432))
    )
    print("connect successfully")
    cur = conn.cursor()

    query = """ 
        SELECT 
            p.id,
            p.name,
            p.price,
            p.quantity,
            f.name,
            f.value,
            c.name,
            c.description,
            i.url,
            d.percentage,
            d.start_date,
            d.end_date
        FROM product p
        LEFT JOIN feature f ON f.product_id = p.id
        LEFT JOIN category c ON c.id = p.category_id
        LEFT JOIN image i ON i.product_id = p.id
        LEFT JOIN discount d ON d.product_id = p.id
        ORDER BY p.id;
    """
    cur.execute(query)
    rows = cur.fetchall()

    products = {}
    for row in rows:
        pid, pname, price, qty, fname, fvalue, cname, cdesc, url,d_start_date,d_end_date,d_percentage = row
        if pid not in products:
            products[pid] = {
                "id": pid,
                "name": pname,
                "price": float(price),
                "quantity": qty,
                "image": url,   # thêm url vào đây
                "category": {
                    "name": cname,
                    "description": cdesc
                },
                "features": [],
            }
            if d_percentage is not None:
                products[pid]["discount"] = {
                    "percentage": d_percentage,
                    "start_date": d_start_date,
                    "end_date": d_end_date
                }
        if fname:
            products[pid]["features"].append({
                "name": fname,
                "value": fvalue
            })
    cur.close()
    conn.close()
    return list(products.values())

def load_orders_from_postgres():
    conn = psycopg2.connect(
        dbname=os.getenv("POSTGRES_DB", "ecommerce"),
        user=os.getenv("POSTGRES_USER", "master"),
        password=os.getenv("POSTGRES_PASSWORD", "123"),
        host=os.getenv("POSTGRES_HOST", "localhost"),
        port=int(os.getenv("POSTGRES_PORT", 5432))
    )
    cur = conn.cursor()
    query = """
        SELECT 
            o.id AS order_id,
            o.created_at,
            o.customer_name,
            o.customer_email,
            o.address,
            o.status,
            o.total_amount,
            oi.id AS item_id,
            oi.quantity,
            oi.price,
            oi.product_id
        FROM orders o
        LEFT JOIN order_item oi ON oi.order_id = o.id
        ORDER BY o.id;
    """
    cur.execute(query)
    rows = cur.fetchall()
    orders = {}
    for r in rows:
        oid = r[0]
        if oid not in orders:
            orders[oid] = {
                "id": oid,
                "createdAt": r[1],
                "customerName": r[2],
                "customerEmail": r[3],
                "address": r[4],
                "status": r[5],
                "totalAmount": r[6],
                "items": []
            }
        
        item_id = r[7]
        if item_id:
            test = orders[oid]["items"].append({
                "id": item_id,
                "quantity": r[8],
                "price": r[9],
                "productId": r[10] if r[10] is not None else ""
            })
    cur.close()
    conn.close()
    return list(orders.values())

# ========= Sync sang Qdrant =========
def sync_products_to_qdrant():
    products = load_products_from_postgres()
    if not products:
        print("⚠️ Không có sản phẩm nào trong Postgres.")
        return
    upsert_products_batch(products)
    print(f"✅ Đã sync {len(products)} sản phẩm vào Qdrant collection '{QDRANT_COLLECTION_PRODUCTS}'")

def sync_orders_to_qdrant():
    orders = load_orders_from_postgres()
    if not orders:
        print("⚠️ Không có order nào trong Postgres.")
        return
    upsert_orders_bath(orders)
    print(f"✅ Đã sync {len(orders)} orders vào Qdrant collection '{QDRANT_COLLECTION_ORDERS}'")

