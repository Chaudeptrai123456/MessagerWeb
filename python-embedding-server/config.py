import os
from dotenv import load_dotenv
load_dotenv()

LOCALAI_URL = "http://localhost:4891/v1/embeddings"
LOCALAI_MODEL = "qwen3-embedding-4b"

# Qdrant config
QDRANT_HOST = "localhost"
QDRANT_HTTP_PORT = 6333
COLLECTION_NAME = "product_vectors"

# Embedding dimension (tự động phát hiện khi lần đầu chạy)
VECTOR_SIZE = int(os.getenv("VECTOR_SIZE", 2560))
# ==========================
# QDRANT CONFIG
# ==========================
# Vì trong Docker Compose, service tên là "qdrant" → host là "qdrant"
QDRANT_HOST = os.getenv("QDRANT_HOST", "qdrant")
QDRANT_HTTP_PORT = int(os.getenv("QDRANT_HTTP_PORT", 6333))
QDRANT_GRPC_PORT = int(os.getenv("QDRANT_GRPC_PORT", 6334))

# Collection lưu vector
COLLECTION_NAME = os.getenv("QDRANT_COLLECTION", "product_vectors")

# ==========================
# EMBEDDING MODEL CONFIG
# ==========================
# Dùng HuggingFace hoặc LocalAI
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "all-MiniLM-L6-v2")

# Nếu Châu đang dùng LocalAI trong Docker Compose
# thì endpoint nó là http://localai:8080/v1/embeddings
LOCALAI_URL = os.getenv("LOCALAI_URL", "http://localai:8080/v1/embeddings")

# ==========================
# VECTOR SIZE (chỉnh đúng với model embedding)
# ==========================
# all-MiniLM-L6-v2 → 384 dimension
# qwen2-embedding → 1536 dimension
# ==========================
# LOG LEVEL
# ==========================
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")
