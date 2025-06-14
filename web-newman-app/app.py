from fastapi import FastAPI, Request, UploadFile, File
from fastapi.responses import HTMLResponse, FileResponse
from fastapi.templating import Jinja2Templates
from fastapi.staticfiles import StaticFiles
import shutil
import subprocess
import uuid
import os

app = FastAPI()
templates = Jinja2Templates(directory="templates")

UPLOAD_FOLDER = "uploads"
REPORT_FOLDER = "static/reports"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(REPORT_FOLDER, exist_ok=True)

app.mount("/static", StaticFiles(directory="static"), name="static")


@app.get("/", response_class=HTMLResponse)
async def read_root(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})


@app.post("/upload", response_class=HTMLResponse)
async def upload_file(request: Request, file: UploadFile = File(...)):
    # 儲存上傳的 collection.json
    uid = str(uuid.uuid4())
    collection_path = f"{UPLOAD_FOLDER}/{uid}.json"
    with open(collection_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    # 執行 newman 測試
    report_path = f"{REPORT_FOLDER}/{uid}.html"
    try:
        subprocess.run([
            "newman", "run", collection_path,
            "--reporters", "html",
            "--reporter-html-export", report_path
        ], check=True)
    except subprocess.CalledProcessError:
        return templates.TemplateResponse("index.html", {
            "request": request,
            "report_url": None,
            "error": "Newman execution failed"
        })

    # 回傳 HTML 路徑
    report_url = f"/static/reports/{uid}.html"
    return templates.TemplateResponse("index.html", {
        "request": request,
        "report_url": report_url
    })