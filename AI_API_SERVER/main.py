from fastapi import FastAPI, Form, UploadFile, File, Request
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from gpt4all import GPT4All
import os, uuid

from vector_db import query_similar, add_document
from pdf_utils import extract_text_from_pdf
from auth_middleware import AuthMiddleware, AUTH_SERVER_URL
app = FastAPI()
templates = Jinja2Templates(directory="templates")

# Đăng ký middleware
# app.add_middleware(AuthMiddleware)



# Load GPT4All model
model_path = r"C:\Users\LENOVO\AppData\Local\nomic.ai\GPT4All\qwen2.5-coder-7b-instruct-q4_0.gguf"
if not os.path.exists(model_path):
    raise FileNotFoundError(f"Không tìm thấy file model tại: {model_path}")
gpt4all = GPT4All(model_path)

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)


@app.get("/", response_class=HTMLResponse)
async def home(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})


@app.get("/login")
async def login():
    """Chuyển user qua trang login của Spring Auth Server"""
    return RedirectResponse(url=f"{AUTH_SERVER_URL}/oauth2/authorization/google")


@app.post("/generate")
async def generate_text(text: str = Form(...)):
    results = query_similar(text, top_k=2)
    context = "\n".join(results.get("documents", [])) if results.get("documents") else ""
    full_prompt = f"{context}\n\nCâu hỏi: {text}"
    with gpt4all.chat_session():
        response = gpt4all.generate(full_prompt, max_tokens=200)
    return {"response": response}


@app.post("/add")
async def add_to_vector_db(id: str = Form(...), content: str = Form(...)):
    add_document(id, content)
    return {"message": f"Đã thêm văn bản với ID: {id}"}


@app.post("/upload_pdf")
async def upload_pdf(id: str = Form(...), file: UploadFile = File(...)):
    file_id = str(uuid.uuid4())
    file_path = os.path.join(UPLOAD_DIR, f"{file_id}_{file.filename}")

    with open(file_path, "wb") as f:
        f.write(await file.read())

    text = extract_text_from_pdf(file_path)
    add_document(id, text)

    return {
        "message": f"Đã lưu nội dung từ PDF với ID: {id}",
        "text_preview": text[:500]
    }
@app.on_event("shutdown")
def shutdown_event():
    gpt4all.close() 