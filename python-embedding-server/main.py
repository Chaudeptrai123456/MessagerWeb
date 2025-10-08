from fastapi import FastAPI, HTTPException,Request
from pydantic import BaseModel
from embedding_service import generate_embedding
from vector_store import upsert_vector, search_similar, init_collection
from config import VECTOR_SIZE
from fastapi.responses import JSONResponse

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
async def embed_text(request: Request):
    data = await request.json()
    text = data.get("text")

    if not text:
        return JSONResponse(content={"error": "Missing text"}, status_code=400)

    vector = model.encode(text).tolist()
    return JSONResponse(content={"text": text, "vector": vector})
@app.post("/search")
def search_similar_products(query: str):
    try:
        # sinh embedding cho query text
        query_vector = generate_embedding({"name": query, "description": query})
        results = search_similar(query_vector, limit=3)
        return {"results": [r.dict() for r in results]}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
