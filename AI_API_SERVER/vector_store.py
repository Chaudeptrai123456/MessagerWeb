import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer
from datetime import datetime

# Khởi tạo ChromaDB local
client = chromadb.Client(Settings(
    persist_directory="./chroma_data",
    anonymized_telemetry=False
))

# Khởi tạo model embedding
embedder = SentenceTransformer("all-MiniLM-L6-v2")

# Tạo hoặc lấy collection theo user
def get_user_collection(user_id: str):
    return client.get_or_create_collection(name=f"user_{user_id}")

# Thêm văn bản vào vector DB
def add_document(user_id: str, doc_id: str, text: str, topic: str = "general"):
    try:
        collection = get_user_collection(user_id)
        embedding = embedder.encode(text).tolist()

        # Kiểm tra tồn tại
        existing = collection.get(ids=[doc_id])
        if existing.get("ids"):
            collection.update(
                ids=[doc_id],
                documents=[text],
                embeddings=[embedding],
                metadatas=[{
                    "user_id": user_id,
                    "topic": topic,
                    "timestamp": datetime.utcnow().isoformat()
                }]
            )
            print(f"[UPDATE] Đã cập nhật văn bản với ID: {doc_id}")
        else:
            collection.add(
                documents=[text],
                ids=[doc_id],
                embeddings=[embedding],
                metadatas=[{
                    "user_id": user_id,
                    "topic": topic,
                    "timestamp": datetime.utcnow().isoformat()
                }]
            )
            print(f"[ADD] Đã thêm văn bản mới với ID: {doc_id}")
    except Exception as e:
        print(f"[ERROR] Khi thêm văn bản: {e}")

# Truy vấn văn bản gần nhất
def query_similar(user_id: str, text: str, top_k: int = 3):
    try:
        collection = get_user_collection(user_id)
        embedding = embedder.encode(text).tolist()
        results = collection.query(
            query_embeddings=[embedding],
            n_results=top_k
        )
        return {
            "ids": results.get("ids", [[]])[0],
            "documents": results.get("documents", [[]])[0],
            "distances": results.get("distances", [[]])[0],
            "metadatas": results.get("metadatas", [[]])[0]
        }
    except Exception as e:
        print(f"[ERROR] Khi truy vấn: {e}")
        return {"error": str(e), "documents": []}
def list_all_collections_data(verbose: bool = False):
    try:
        all_collections = client.list_collections()
        result = {}

        for col in all_collections:
            name = col.name
            collection = client.get_collection(name=name)
            ids = collection.get()["ids"]

            if not ids:
                result[name] = {
                    "total": 0,
                    "documents": [],
                    "metadatas": [],
                    "ids": [],
                    "config": col.metadata
                }
                continue

            data = collection.get(ids=ids)
            docs = data["documents"]
            metas = data["metadatas"]

            # Nếu verbose, trích thêm thông tin từ văn bản
            if verbose:
                enriched = []
                for i, doc in enumerate(docs):
                    latest = ""
                    trend = ""
                    try:
                        lines = doc.splitlines()
                        for line in lines:
                            if "Latest Value:" in line:
                                latest = line.strip()
                            if "Historical Trend:" in line:
                                trend = "\n".join(lines[lines.index(line)+1:lines.index(line)+6])
                    except:
                        pass
                    enriched.append({
                        "id": data["ids"][i],
                        "latest_value": latest,
                        "trend": trend,
                        "metadata": metas[i]
                    })
                result[name] = {
                    "total": len(ids),
                    "summary": enriched,
                    "config": col.metadata
                }
            else:
                result[name] = {
                    "total": len(ids),
                    "documents": docs,
                    "metadatas": metas,
                    "ids": data["ids"],
                    "config": col.metadata
                }

        return result
    except Exception as e:
        return {"error": str(e)}
def add_document(user_id: str, doc_id: str, text: str, topic: str):
    embedding = embedder.encode(text).tolist()
    collection = client.get_or_create_collection(name=f"user_{user_id}")
    collection.add(
        documents=[text],
        ids=[doc_id],
        embeddings=[embedding],
        metadatas=[{
            "user_id": user_id,
            "topic": topic,
            "timestamp": datetime.utcnow().isoformat(),
            "needs_training": True
        }]
    )


