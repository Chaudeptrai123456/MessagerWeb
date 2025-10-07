import requests


def generate_embedding(product):
    """
    Gọi LocalAI để sinh embedding cho product.
    product: dict có các trường name, description, category, features.
    """
    url = "http://localhost:4891/v1/embeddings"
    model_name = "qwen3-embedding-4b"

    parts = []
    if product.get("name"):
        parts.append(product["name"])
    if product.get("description"):
        parts.append(product["description"])

    category = product.get("category")
    if category and category.get("name"):
        parts.append(f"Category: {category['name']}")

    features = product.get("features", [])
    for f in features:
        if f.get("name"):
            parts.append(f"Feature: {f['name']}")
        if f.get("value"):
            parts.append(f"{f['value']}")

    content = " ".join(parts).strip()

    payload = {"model": model_name, "input": content}
    headers = {"Content-Type": "application/json"}

    response = requests.post(url, json=payload, headers=headers)
    if not response.ok:
        raise Exception(f"LocalAI lỗi: {response.status_code} - {response.text}")

    data = response.json()
    if "data" not in data or not data["data"]:
        raise Exception("Không nhận được 'data' từ LocalAI!")

    embedding = data["data"][0]["embedding"]
    return embedding
