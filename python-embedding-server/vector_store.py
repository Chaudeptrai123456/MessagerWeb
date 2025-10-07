from qdrant_client import QdrantClient, models
from config import QDRANT_HOST, QDRANT_HTTP_PORT, COLLECTION_NAME
import uuid

# Kết nối Qdrant
client = QdrantClient(host=QDRANT_HOST, port=QDRANT_HTTP_PORT)

# Tạo collection nếu chưa có
def init_collection(vector_size=1536):
    collections = client.get_collections().collections
    if not any(c.name == COLLECTION_NAME for c in collections):
        print(f"🆕 Creating collection '{COLLECTION_NAME}'...")
        client.create_collection(
            collection_name=COLLECTION_NAME,
            vectors_config=models.VectorParams(size=vector_size, distance=models.Distance.COSINE),
        )

def upsert_vector(text_id, vector, metadata=None):
    if isinstance(text_id, str):
        text_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, text_id))  # convert sang UUID

    payload = metadata or {}
    client.upsert(
        collection_name=COLLECTION_NAME,
        points=[
            models.PointStruct(
                id=text_id,
                vector=vector,
                payload=payload
            )
        ]
    )
    return vector


def search_similar(query_vector, limit=3):
    res = client.search(
        collection_name=COLLECTION_NAME,
        query_vector=query_vector,
        limit=limit
    )
    return res
