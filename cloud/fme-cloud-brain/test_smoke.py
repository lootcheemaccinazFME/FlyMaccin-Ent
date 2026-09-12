import os
import tempfile

_db = tempfile.NamedTemporaryFile(prefix="fme-cloud-brain-", suffix=".db", delete=False)
_db.close()
os.environ["APP_ENV"] = "test"
os.environ["FME_API_KEY"] = "test-secret"
os.environ["DATABASE_PATH"] = _db.name
os.environ["RAG_TOP_K"] = "2"

from fastapi.testclient import TestClient  # noqa: E402
from main import app  # noqa: E402


def auth():
    return {"Authorization": "Bearer test-secret"}


def test_health():
    with TestClient(app) as client:
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"
        assert data["version"] == "0.1.0"


def test_auth_required():
    with TestClient(app) as client:
        response = client.get("/v1/fme/library/search", params={"q": "canon"})
        assert response.status_code == 401


def test_library_ingest_and_search():
    with TestClient(app) as client:
        create = client.post(
            "/v1/fme/library/documents",
            headers=auth(),
            json={
                "title": "Test FME Canon",
                "content": "The cloud brain uses authoritative FME library context.",
                "tags": ["canon", "test"],
                "authoritative": True,
            },
        )
        assert create.status_code == 200
        result = create.json()
        assert result["authoritative"] is True

        search = client.get(
            "/v1/fme/library/search",
            headers=auth(),
            params={"q": "authoritative library context"},
        )
        assert search.status_code == 200
        rows = search.json()["results"]
        assert rows
        assert rows[0]["title"] == "Test FME Canon"


def test_models_endpoint():
    with TestClient(app) as client:
        response = client.get("/v1/models", headers=auth())
        assert response.status_code == 200
        assert response.json()["data"][0]["owned_by"] == "flymaccin-ent"
