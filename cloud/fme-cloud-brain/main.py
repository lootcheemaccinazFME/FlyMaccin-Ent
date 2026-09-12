import os
import re
import sqlite3
import uuid
from contextlib import asynccontextmanager
from typing import Any, Literal

import httpx
from fastapi import Depends, FastAPI, Header, HTTPException, Query, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

APP_NAME = "FME Cloud Brain"
APP_VERSION = "0.1.0"
APP_ENV = os.getenv("APP_ENV", "development").lower()
FME_API_KEY = os.getenv("FME_API_KEY", "")
MODEL_BASE_URL = os.getenv("MODEL_BASE_URL", "http://model:8000/v1").rstrip("/")
MODEL_API_KEY = os.getenv("MODEL_API_KEY", "local")
MODEL_NAME = os.getenv("MODEL_NAME", "fme-local")
DATABASE_PATH = os.getenv("DATABASE_PATH", "/data/fme.db")
RAG_TOP_K = max(0, min(int(os.getenv("RAG_TOP_K", "6")), 20))
SYSTEM_PROMPT = os.getenv(
    "FME_SYSTEM_PROMPT",
    "You are the FME Cloud Brain, a private AI service for FlyMaccin Ent. "
    "Use authoritative FME library context when supplied. Never invent canon when the library does not support it.",
)
ALLOWED_ORIGINS = [x.strip() for x in os.getenv("ALLOWED_ORIGINS", "").split(",") if x.strip()]

if APP_ENV == "production" and not FME_API_KEY:
    raise RuntimeError("FME_API_KEY is required when APP_ENV=production")


def db_connect() -> sqlite3.Connection:
    parent = os.path.dirname(DATABASE_PATH)
    if parent:
        os.makedirs(parent, exist_ok=True)
    conn = sqlite3.connect(DATABASE_PATH, check_same_thread=False)
    conn.row_factory = sqlite3.Row
    return conn


def init_db() -> None:
    with db_connect() as conn:
        conn.execute(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS fme_documents USING fts5(
                id UNINDEXED,
                title,
                content,
                tags,
                authoritative UNINDEXED,
                tokenize='unicode61'
            )
            """
        )
        conn.commit()


def build_fts_query(text: str) -> str:
    tokens = re.findall(r"[\w'-]+", text, flags=re.UNICODE)[:24]
    return " OR ".join(f'"{t.replace(chr(34), "")}"' for t in tokens)


def search_library(query: str, limit: int) -> list[dict[str, Any]]:
    fts_query = build_fts_query(query)
    if not fts_query or limit <= 0:
        return []
    with db_connect() as conn:
        rows = conn.execute(
            """
            SELECT id, title, content, tags, authoritative, bm25(fme_documents) AS score
            FROM fme_documents
            WHERE fme_documents MATCH ?
            ORDER BY score
            LIMIT ?
            """,
            (fts_query, limit),
        ).fetchall()
    return [dict(r) for r in rows]


def require_api_key(authorization: str | None = Header(default=None)) -> None:
    if not FME_API_KEY and APP_ENV != "production":
        return
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")
    token = authorization[7:]
    if token != FME_API_KEY:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid bearer token")


class Message(BaseModel):
    role: Literal["system", "user", "assistant", "tool"]
    content: str


class ChatRequest(BaseModel):
    model: str | None = None
    messages: list[Message] = Field(min_length=1)
    temperature: float | None = Field(default=0.7, ge=0, le=2)
    max_tokens: int | None = Field(default=1024, ge=1, le=32768)
    stream: bool = False


class DocumentIn(BaseModel):
    title: str = Field(min_length=1, max_length=300)
    content: str = Field(min_length=1)
    tags: list[str] = Field(default_factory=list)
    authoritative: bool = True


class DocumentOut(BaseModel):
    id: str
    title: str
    authoritative: bool


@asynccontextmanager
async def lifespan(_: FastAPI):
    init_db()
    yield


app = FastAPI(title=APP_NAME, version=APP_VERSION, lifespan=lifespan)

if ALLOWED_ORIGINS:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=ALLOWED_ORIGINS,
        allow_credentials=False,
        allow_methods=["GET", "POST"],
        allow_headers=["Authorization", "Content-Type"],
    )


@app.middleware("http")
async def security_headers(request: Request, call_next):
    response = await call_next(request)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["Referrer-Policy"] = "no-referrer"
    response.headers["Cache-Control"] = "no-store"
    return response


@app.get("/health")
def health() -> dict[str, Any]:
    init_db()
    return {"status": "ok", "service": APP_NAME, "version": APP_VERSION, "environment": APP_ENV}


@app.get("/ready")
async def ready(_: None = Depends(require_api_key)) -> dict[str, Any]:
    init_db()
    headers = {"Authorization": f"Bearer {MODEL_API_KEY}"}
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            r = await client.get(f"{MODEL_BASE_URL}/models", headers=headers)
            r.raise_for_status()
        return {"status": "ready", "model_backend": MODEL_BASE_URL, "model": MODEL_NAME}
    except Exception as exc:
        raise HTTPException(status_code=503, detail=f"Model backend not ready: {type(exc).__name__}") from exc


@app.get("/v1/models")
def models(_: None = Depends(require_api_key)) -> dict[str, Any]:
    return {
        "object": "list",
        "data": [{"id": MODEL_NAME, "object": "model", "owned_by": "flymaccin-ent"}],
    }


@app.post("/v1/fme/library/documents", response_model=DocumentOut)
def add_document(doc: DocumentIn, _: None = Depends(require_api_key)) -> DocumentOut:
    doc_id = str(uuid.uuid4())
    with db_connect() as conn:
        conn.execute(
            "INSERT INTO fme_documents(id, title, content, tags, authoritative) VALUES (?, ?, ?, ?, ?)",
            (doc_id, doc.title, doc.content, ",".join(doc.tags), "1" if doc.authoritative else "0"),
        )
        conn.commit()
    return DocumentOut(id=doc_id, title=doc.title, authoritative=doc.authoritative)


@app.get("/v1/fme/library/search")
def library_search(
    q: str = Query(min_length=1, max_length=1000),
    limit: int = Query(default=6, ge=1, le=20),
    _: None = Depends(require_api_key),
) -> dict[str, Any]:
    return {"query": q, "results": search_library(q, limit)}


def enriched_messages(messages: list[Message]) -> list[dict[str, str]]:
    last_user = next((m.content for m in reversed(messages) if m.role == "user"), "")
    hits = search_library(last_user, RAG_TOP_K) if last_user else []
    context = "\n\n".join(
        f"[FME LIBRARY: {hit['title']} | authoritative={hit['authoritative']}]\n{hit['content']}" for hit in hits
    )
    system = SYSTEM_PROMPT
    if context:
        system += "\n\nAuthoritative/retrieved FME context:\n" + context
    output = [{"role": "system", "content": system}]
    output.extend({"role": m.role, "content": m.content} for m in messages)
    return output


@app.post("/v1/chat/completions")
async def chat(req: ChatRequest, _: None = Depends(require_api_key)):
    payload = {
        "model": req.model or MODEL_NAME,
        "messages": enriched_messages(req.messages),
        "temperature": req.temperature,
        "max_tokens": req.max_tokens,
        "stream": req.stream,
    }
    headers = {"Authorization": f"Bearer {MODEL_API_KEY}", "Content-Type": "application/json"}

    if req.stream:
        client = httpx.AsyncClient(timeout=None)
        upstream = await client.send(
            client.build_request("POST", f"{MODEL_BASE_URL}/chat/completions", json=payload, headers=headers),
            stream=True,
        )
        if upstream.status_code >= 400:
            body = await upstream.aread()
            await upstream.aclose()
            await client.aclose()
            raise HTTPException(status_code=502, detail=body.decode("utf-8", "replace")[:1000])

        async def relay():
            try:
                async for chunk in upstream.aiter_bytes():
                    yield chunk
            finally:
                await upstream.aclose()
                await client.aclose()

        return StreamingResponse(relay(), media_type=upstream.headers.get("content-type", "text/event-stream"))

    try:
        async with httpx.AsyncClient(timeout=120) as client:
            upstream = await client.post(f"{MODEL_BASE_URL}/chat/completions", json=payload, headers=headers)
            upstream.raise_for_status()
            return upstream.json()
    except httpx.HTTPStatusError as exc:
        detail = exc.response.text[:1000]
        raise HTTPException(status_code=502, detail=f"Model backend error: {detail}") from exc
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=503, detail=f"Model backend unavailable: {type(exc).__name__}") from exc
