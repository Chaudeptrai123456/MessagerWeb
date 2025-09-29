import fitz  # PyMuPDF

def extract_text_from_pdf(file_path: str) -> str:
    """
    Trích xuất text từ file PDF bằng PyMuPDF.
    """
    doc = fitz.open(file_path)
    text = ""
    for page in doc:
        text += page.get_text("text")  # lấy text thuần
    return text
