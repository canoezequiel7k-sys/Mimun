import pytest
from fastapi.testclient import TestClient

from app.infrastructure.config import get_settings
from app.main import create_app


def test_health_returns_ok(client: TestClient) -> None:
    response = client.get("/api/v1/health")

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_cors_allows_only_configured_origins(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setenv("CORS_ORIGINS", '["https://mimun.example"]')
    get_settings.cache_clear()
    try:
        with TestClient(create_app()) as test_client:
            preflight = test_client.options(
                "/api/v1/auth/login",
                headers={
                    "Origin": "https://mimun.example",
                    "Access-Control-Request-Method": "POST",
                    "Access-Control-Request-Headers": "authorization,content-type",
                },
            )
            assert preflight.status_code == 200
            assert preflight.headers["access-control-allow-origin"] == "https://mimun.example"

            rejected = test_client.get(
                "/api/v1/health", headers={"Origin": "https://other.example"}
            )
            assert "access-control-allow-origin" not in rejected.headers
    finally:
        get_settings.cache_clear()
