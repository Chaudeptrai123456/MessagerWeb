# vector_store.py
from qdrant_client import QdrantClient
from qdrant_client.http import models
from sentence_transformers import SentenceTransformer
from datetime import datetime

# ==============================
# Kết nối Qdrant
# ==============================
qdrant = QdrantClient(url="http://localhost:6333")

# Model embedding
embedder = SentenceTransformer("all-MiniLM-L6-v2")
VECTOR_DIM = 384  # all-MiniLM-L6-v2 dimension


# ==============================
# Tạo / lấy collection theo user
# ==============================
def get_user_collection(user_id: str):
    collection_name = f"user_{user_id}"
    try:
        qdrant.get_collection(collection_name)
    except:
        qdrant.recreate_collection(
            collection_name=collection_name,
            vectors_config=models.VectorParams(size=VECTOR_DIM, distance=models.Distance.COSINE)
        )
    return collection_name


# ==============================
# Thêm hoặc cập nhật document
# ==============================
def add_or_update_document(user_id: str, doc_id: str, text: str, topic: str = "general"):
    try:
        collection_name = get_user_collection(user_id)
        embedding = embedder.encode(text).tolist()

        qdrant.upsert(
            collection_name=collection_name,
            points=[
                models.PointStruct(
                    id=doc_id,
                    vector=embedding,
                    payload={
                        "user_id": user_id,
                        "topic": topic,
                        "text": text,
                        "timestamp": datetime.utcnow().isoformat(),
                        "needs_training": True
                    }
                )
            ]
        )
        print(f"[UPSERT] {doc_id} vào collection {collection_name}")
    except Exception as e:
        print(f"[ERROR] Khi thêm/cập nhật văn bản: {e}")


# ==============================
# Query document gần nhất
# ==============================
def query_similar(user_id: str, text: str, top_k: int = 3):
    try:
        collection_name = get_user_collection(user_id)
        embedding = embedder.encode(text).tolist()

        results = qdrant.search(
            collection_name=collection_name,
            query_vector=embedding,
            limit=top_k
        )

        return [
            {
                "id": r.id,
                "score": r.score,
                "text": r.payload.get("text", ""),
                "metadata": r.payload
            }
            for r in results
        ]
    except Exception as e:
        print(f"[ERROR] Khi truy vấn: {e}")
        return []


# ==============================
# Liệt kê tất cả collections và dữ liệu
# ==============================
def list_all_collections_data():
    try:
        collections = qdrant.get_collections().collections
        result = {}

        for col in collections:
            name = col.name
            points, _ = qdrant.scroll(collection_name=name, limit=100)
            result[name] = {
                "total": len(points),
                "documents": [p.payload.get("text") for p in points],
                "metadatas": [p.payload for p in points],
                "ids": [p.id for p in points]
            }
        return result
    except Exception as e:
        return {"error": str(e)}
def add_document(user_id: str, doc_id: str, text: str, topic: str = "general"):
    return add_or_update_document(user_id, doc_id, text, topic)
