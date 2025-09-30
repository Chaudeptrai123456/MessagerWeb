# fred_ingest.py
from fredapi import Fred
from datetime import datetime
import os
import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer

client = chromadb.Client(Settings(persist_directory="./chroma_data", anonymized_telemetry=False))
collection = client.get_or_create_collection(name="fred_data")
embedder = SentenceTransformer("all-MiniLM-L6-v2")
fred = Fred(api_key="fb781f5101b8fdaffc2f4ca1d6a909ad")  # Gán trực tiếp hoặc dùng biến môi trường

def ingest_fred_series(series_id: str, doc_prefix: str = "fred"):
    try:
        data = fred.get_series(series_id)
        metadata = fred.get_series_info(series_id)
        title = metadata.get("title", "No title")
        unit = metadata.get("units", "")
        freq = metadata.get("frequency", "")
        source = metadata.get("source", "")
        text = f"""
        Series: {title}
        Frequency: {freq}
        Unit: {unit}
        Source: {source}
        Latest Value: {data.iloc[-1]:.2f}
        Historical Trend:
        {data.tail(10).to_string()}
        """
        doc_id = f"{doc_prefix}_{series_id}"
        embedding = embedder.encode(text).tolist()

        # Kiểm tra xem doc_id đã tồn tại chưa
        existing = collection.get(ids=[doc_id])
        if existing.get("ids"):
            collection.update(
                ids=[doc_id],
                documents=[text],
                embeddings=[embedding],
                metadatas=[{
                    "series_id": series_id,
                    "title": title,
                    "timestamp": datetime.utcnow().isoformat()
                }]
            )
            print(f"[🔄 UPDATED] {series_id} → {doc_id}")
        else:
            collection.add(
                documents=[text],
                ids=[doc_id],
                embeddings=[embedding],
                metadatas=[{
                    "series_id": series_id,
                    "title": title,
                    "timestamp": datetime.utcnow().isoformat()
                }]
            )
            print(f"[✅ INGESTED] {series_id} → {doc_id}")
    except Exception as e:
        print(f"[❌ ERROR] Khi lấy dữ liệu FRED: {e}")

def query_macro_context(text: str, top_k: int = 3):
    try:
        # Dùng embedding để truy vấn trong collection "fred_data"
        embedding = embedder.encode(text).tolist()
        collection = client.get_collection(name="fred_data")
        results = collection.query(
            query_embeddings=[embedding],
            n_results=top_k
        )
        context = "\n".join(results.get("documents", [[]])[0])
        return context
    except Exception as e:
        print(f"[ERROR] Khi truy vấn macro context: {e}")
        return ""
def ingest_default_series():
    for series in ["FEDFUNDS", "CPIAUCSL", "GDP"]:
        ingest_fred_series(series)
