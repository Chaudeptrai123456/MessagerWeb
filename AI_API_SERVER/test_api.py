import requests

url = "http://127.0.0.1:8000/generate"
data = {
    "text": "Viết một đoạn văn giới thiệu bản thân bằng tiếng Việt."
}

response = requests.post(url, json=data)
print("Kết quả GPT4All trả về:")
print(response.json())
