from fastapi import FastAPI, Form, UploadFile, File, Request
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from vector_store import list_all_collections_data
from training_export import export_training_data
from auto_train import auto_train
from vector_store import add_document, query_similar
from apscheduler.schedulers.background import BackgroundScheduler
from gpt4all import GPT4All
import os, uuid
from fred_ingest import ingest_default_series
from vector_db import query_similar, add_document
from pdf_utils import extract_text_from_pdf
from auth_middleware import AuthMiddleware, AUTH_SERVER_URL
app = FastAPI()
templates = Jinja2Templates(directory="templates")
from vector_store import add_document, query_similar
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
async def generate_text(request: Request, text: str = Form(...)): 
    user_id = request.cookies.get("user_id")  # hoặc từ token
    if  not user_id:
        user_id = "guest"  # user mặc định nếu không có đăng nhập
    results = query_similar(user_id, text, top_k=2)
    context = "\n".join(results.get("documents", [])) if results.get("documents") else ""
    full_prompt = f"{context}\n\nCâu hỏi: {text}"

    with gpt4all.chat_session():
        response = gpt4all.generate(full_prompt, max_tokens=200)

    # Lưu lại prompt + response
    add_document(user_id, doc_id=str(uuid.uuid4()), text=text + "\n\n" + response, topic="chat")

    return {"response": response}
from vector_store import query_similar
from fred_ingest import query_macro_context

@app.post("/generate")
async def generate_text_finance(request: Request, text: str = Form(...)): 
    user_id = request.cookies.get("user_id") or "guest"

    # Truy vấn lịch sử người dùng
    user_context = query_similar(user_id, text, top_k=2)
    user_history = "\n".join(user_context.get("documents", [])) if user_context.get("documents") else ""

    # Truy vấn dữ liệu vĩ mô liên quan
    macro_context = query_macro_context(text)

    # Tạo prompt đầy đủ
    full_prompt = f"""
    Dữ liệu vĩ mô liên quan:\n{macro_context}
    Lịch sử tương tác:\n{user_history}
    Câu hỏi hiện tại:\n{text}
    """

    with gpt4all.chat_session():
        response = gpt4all.generate(full_prompt, max_tokens=300)

    # Lưu lại prompt + response
    add_document(user_id, doc_id=str(uuid.uuid4()), text=text + "\n\n" + response, topic="chat")

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
    try:
        gpt4all.close()
        print("[SHUTDOWN] GPT4All đã đóng.")
    except Exception as e:
        print(f"[ERROR] Khi shutdown: {e}")

@app.on_event("startup")
def startup_event():
    print("[STARTUP] Đang tải dữ liệu FRED...")
    ingest_default_series()
@app.post("/update_fred")
async def update_fred_data():
    ingest_default_series()
    return {"message": "Đã cập nhật dữ liệu FRED vào vector DB."}

@app.get("/vector_dump")
async def vector_dump(request: Request):
    results = list_all_collections_data(verbose=True)
    return results
@app.post("/export_training")
async def export_training():
    export_training_data()
    return {"message": "Đã xuất dữ liệu huấn luyện từ vector DB"}
@app.on_event("startup")
def startup_event():
    scheduler = BackgroundScheduler()
    scheduler.add_job(auto_train, "interval", hours=6)
    scheduler.start()
