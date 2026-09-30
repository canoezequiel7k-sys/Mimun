import datetime as dt
from collections.abc import Iterator
from uuid import uuid4

import pytest
from fastapi.testclient import TestClient

from app.application.use_cases.mood_entries import MoodEntryUseCases
from app.infrastructure.config import get_settings
from app.infrastructure.security.jwt_token_service import JwtTokenService
from app.main import create_app
from app.presentation.api.deps import get_mood_entry_use_cases
from tests.fakes import FakeClock, FakeMoodEntryRepository

PROTECTED = ["/api/v1/mood-entries", "/api/v1/journal-entries"]


@pytest.fixture
def client(monkeypatch: pytest.MonkeyPatch) -> Iterator[TestClient]:
    monkeypatch.setenv("AUTH_ENABLED", "true")
    get_settings.cache_clear()
    with TestClient(create_app()) as test_client:
        yield test_client
    get_settings.cache_clear()


def _token(minutes: int = 30) -> str:
    service = JwtTokenService(
        secret_key=get_settings().secret_key, access_token_minutes=minutes, refresh_token_days=14
    )
    return service.create_access_token(uuid4())


@pytest.mark.parametrize("path", PROTECTED)
def test_missing_token_returns_401(client: TestClient, path: str) -> None:
    response = client.get(path)
    assert response.status_code == 401
    assert response.json()["error"]["code"] == "UNAUTHORIZED"
    assert response.headers["WWW-Authenticate"] == "Bearer"


@pytest.mark.parametrize("path", PROTECTED)
def test_garbage_and_expired_tokens_return_401(client: TestClient, path: str) -> None:
    for token in ("basura", _token(minutes=-1)):
        response = client.get(path, headers={"Authorization": f"Bearer {token}"})
        assert response.status_code == 401


def test_valid_token_is_accepted(client: TestClient) -> None:
    use_cases = MoodEntryUseCases.build(
        FakeMoodEntryRepository(), FakeClock(dt.datetime(2026, 9, 30, tzinfo=dt.UTC))
    )
    client.app.dependency_overrides[get_mood_entry_use_cases] = lambda: use_cases
    response = client.get("/api/v1/mood-entries", headers={"Authorization": f"Bearer {_token()}"})
    assert response.status_code == 200
    assert response.json()["total"] == 0


def test_health_stays_public(client: TestClient) -> None:
    assert client.get("/api/v1/health").status_code == 200
