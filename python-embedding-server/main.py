from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from embedding_service import generate_embedding
from vector_store import upsert_vector, search_similar, init_collection
from config import VECTOR_SIZE

app = FastAPI(title="Python Embedding Server")

# 🔹 Khởi tạo Qdrant collection khi server khởi động
init_collection(VECTOR_SIZE)

# -----------------------------
# MODELS
# -----------------------------
class Feature(BaseModel):
    name: str
    value: str | None = None

class Category(BaseModel):
    name: str

class Product(BaseModel):
    id: str
    name: str
    description: str | None = None
    category: Category | None = None
    features: list[Feature] | None = None


# -----------------------------
# ENDPOINTS
# -----------------------------
@app.post("/embed")
def embed_and_store(product: Product):
    try:
        vector = generate_embedding(product.dict())
        upsert_vector(product.id, vector, metadata=product.dict())
        return {"status": "ok", "vector_dim": len(vector)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/search")
def search_similar_products(query: str):
    try:
        # sinh embedding cho query text
        query_vector = generate_embedding({"name": query, "description": query})
        results = search_similar(query_vector, limit=3)
        return {"results": [r.dict() for r in results]}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
