import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer

# Khởi tạo ChromaDB local
client = chromadb.Client(Settings(
    persist_directory="./chroma_data",
    anonymized_telemetry=False
))

# Tạo hoặc lấy collection
collection = client.get_or_create_collection(name="my_collection")

# Khởi tạo model embedding
embedder = SentenceTransformer("all-MiniLM-L6-v2")

# Thêm văn bản vào vector DB
def add_document(doc_id: str, text: str):
    try:
        existing = collection.get(ids=[doc_id])
        if existing.get("ids"):
            # Nếu đã tồn tại → cập nhật
            embedding = embedder.encode(text).tolist()
            collection.update(
                ids=[doc_id],
                documents=[text],
                embeddings=[embedding]
            )
            print(f"[UPDATE] Đã cập nhật văn bản với ID: {doc_id}")
        else:
            # Nếu chưa có → thêm mới
            embedding = embedder.encode(text).tolist()
            collection.add(
                documents=[text],
                ids=[doc_id],
                embeddings=[embedding]
            )
            print(f"[ADD] Đã thêm văn bản mới với ID: {doc_id}")
    except Exception as e:
        print(f"[ERROR] Khi thêm văn bản: {e}")

# Truy vấn văn bản gần nhất
def query_similar(text: str, top_k: int = 3):
    try:
        embedding = embedder.encode(text).tolist()
        results = collection.query(
            query_embeddings=[embedding],
            n_results=top_k
        )
        return {
            "ids": results.get("ids", [[]])[0],
            "documents": results.get("documents", [[]])[0],
            "distances": results.get("distances", [[]])[0]
        }
    except Exception as e:
        print(f"[ERROR] Khi truy vấn: {e}")
        return {"error": str(e), "documents": []}