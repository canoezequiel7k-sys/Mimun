import datetime as dt
from collections.abc import Iterator
from uuid import uuid4

import pytest
from fastapi.testclient import TestClient

from app.application.use_cases.mood_entries import MoodEntryUseCases
from app.main import create_app
from app.presentation.api.deps import get_current_user_id, get_mood_entry_use_cases
from tests.fakes import FakeClock, FakeMoodEntryRepository

NOW = dt.datetime(2026, 9, 29, 14, 30, tzinfo=dt.UTC)
USER_ID = uuid4()
BASE = "/api/v1/mood-entries"


@pytest.fixture
def client() -> Iterator[TestClient]:
    app = create_app()
    use_cases = MoodEntryUseCases.build(FakeMoodEntryRepository(), FakeClock(NOW))
    app.dependency_overrides[get_current_user_id] = lambda: USER_ID
    app.dependency_overrides[get_mood_entry_use_cases] = lambda: use_cases
    with TestClient(app) as test_client:
        yield test_client


def _create(client: TestClient, **overrides: object):
    body = {"date": "2026-09-29", "mood": "GOOD", "note": "Buen día"} | overrides
    return client.post(BASE, json=body)


def test_create_returns_201_with_contract_shape(client: TestClient) -> None:
    response = _create(client)
    assert response.status_code == 201
    data = response.json()
    assert set(data) == {
        "id",
        "date",
        "mood",
        "note",
        "created_at",
        "updated_at",
        "edited_at",
        "deleted_at",
    }
    assert data["date"] == "2026-09-29"
    assert data["mood"] == "GOOD"
    assert data["note"] == "Buen día"
    assert data["created_at"] == "2026-09-29T14:30:00Z"


def test_create_accepts_client_generated_id(client: TestClient) -> None:
    given = str(uuid4())
    assert _create(client, id=given).json()["id"] == given


def test_duplicate_date_returns_409(client: TestClient) -> None:
    _create(client)
    response = _create(client)
    assert response.status_code == 409
    assert response.json()["error"]["code"] == "MOOD_ENTRY_ALREADY_EXISTS"


def test_normal_mood_is_rejected(client: TestClient) -> None:
    response = _create(client, mood="NORMAL")
    assert response.status_code == 422
    error = response.json()["error"]
    assert error["code"] == "VALIDATION_ERROR"
    assert error["details"][0]["field"] == "mood"


def test_unknown_field_is_rejected(client: TestClient) -> None:
    assert _create(client, timestamp="2026-09-29T10:00:00").status_code == 422


def test_note_too_long_is_rejected(client: TestClient) -> None:
    response = _create(client, note="a" * 501)
    assert response.status_code == 422
    assert response.json()["error"]["details"][0]["field"] == "note"


def test_date_too_far_in_the_future_is_rejected(client: TestClient) -> None:
    response = _create(client, date="2026-10-02")
    assert response.status_code == 422
    assert response.json()["error"]["details"][0]["field"] == "date"


def test_list_uses_the_pagination_envelope_and_range(client: TestClient) -> None:
    for day in ("2026-09-27", "2026-09-28", "2026-09-29"):
        _create(client, date=day)
    response = client.get(BASE, params={"from": "2026-09-28", "limit": 10})
    assert response.status_code == 200
    data = response.json()
    assert data["total"] == 2 and data["limit"] == 10 and data["offset"] == 0
    assert [item["date"] for item in data["items"]] == ["2026-09-29", "2026-09-28"]


def test_list_rejects_inverted_range(client: TestClient) -> None:
    response = client.get(BASE, params={"from": "2026-09-29", "to": "2026-09-01"})
    assert response.status_code == 422


def test_get_unknown_id_returns_404_in_unified_format(client: TestClient) -> None:
    response = client.get(f"{BASE}/{uuid4()}")
    assert response.status_code == 404
    assert response.json()["error"]["code"] == "NOT_FOUND"


def test_invalid_uuid_in_path_returns_422(client: TestClient) -> None:
    assert client.get(f"{BASE}/no-es-un-uuid").status_code == 422


def test_put_replaces_mood_and_clears_note_but_keeps_date(client: TestClient) -> None:
    created = _create(client).json()
    response = client.put(f"{BASE}/{created['id']}", json={"mood": "MEH"})
    assert response.status_code == 200
    data = response.json()
    assert data["mood"] == "MEH" and data["note"] is None and data["date"] == "2026-09-29"


def test_delete_returns_204_then_404(client: TestClient) -> None:
    created = _create(client).json()
    assert client.delete(f"{BASE}/{created['id']}").status_code == 204
    assert client.get(f"{BASE}/{created['id']}").status_code == 404


def test_unknown_route_uses_unified_error_format(client: TestClient) -> None:
    response = client.get("/api/v1/nope")
    assert response.status_code == 404
    assert response.json()["error"]["code"] == "NOT_FOUND"
