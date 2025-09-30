# training_export.py
import json
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer
import chromadb

client = chromadb.Client(Settings(persist_directory="./chroma_data", anonymized_telemetry=False))
embedder = SentenceTransformer("all-MiniLM-L6-v2")

def export_training_data(user_prefix: str = "user_", output_file: str = "training_data.jsonl"):
    try:
        all_collections = client.list_collections()
        samples = []

        for col in all_collections:
            if not col.name.startswith(user_prefix):
                continue

            collection = client.get_collection(name=col.name)
            ids = collection.get()["ids"]
            if not ids:
                continue

            data = collection.get(ids=ids)
            for i in range(len(data["documents"])):
                doc = data["documents"][i]
                meta = data["metadatas"][i]
                if "chat" in meta.get("topic", ""):
                    parts = doc.split("\n\n")
                    if len(parts) >= 2:
                        samples.append({
                            "instruction": parts[0].strip(),
                            "output": parts[1].strip(),
                            "metadata": meta
                        })

        with open(output_file, "w", encoding="utf-8") as f:
            for item in samples:
                f.write(json.dumps(item, ensure_ascii=False) + "\n")

        print(f"[✅ EXPORT] Đã xuất {len(samples)} mẫu huấn luyện vào {output_file}")
    except Exception as e:
        print(f"[❌ ERROR] Khi export dữ liệu huấn luyện: {e}")
