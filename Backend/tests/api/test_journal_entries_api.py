import datetime as dt
from collections.abc import Iterator
from uuid import uuid4

import pytest
from fastapi.testclient import TestClient

from app.application.use_cases.journal_entries import JournalEntryUseCases
from app.domain.entities import MoodEntry
from app.domain.value_objects import MoodType
from app.main import create_app
from app.presentation.api.deps import get_current_user_id, get_journal_entry_use_cases
from tests.fakes import FakeClock, FakeJournalEntryRepository, FakeMoodEntryRepository

NOW = dt.datetime(2026, 9, 29, 21, 0, tzinfo=dt.UTC)
USER_ID = uuid4()
BASE = "/api/v1/journal-entries"


@pytest.fixture
def mood_repo() -> FakeMoodEntryRepository:
    return FakeMoodEntryRepository()


@pytest.fixture
def client(mood_repo: FakeMoodEntryRepository) -> Iterator[TestClient]:
    app = create_app()
    use_cases = JournalEntryUseCases.build(FakeJournalEntryRepository(), mood_repo, FakeClock(NOW))
    app.dependency_overrides[get_current_user_id] = lambda: USER_ID
    app.dependency_overrides[get_journal_entry_use_cases] = lambda: use_cases
    with TestClient(app) as test_client:
        yield test_client


def _mood_entry_id(repo: FakeMoodEntryRepository) -> str:
    entry = MoodEntry.create(
        user_id=USER_ID, date=dt.date(2026, 9, 29), mood=MoodType.GOOD, note=None, now=NOW
    )
    repo.add(entry)
    return str(entry.id)


def _create(client: TestClient, **overrides: object):
    body = {"title": "Sobre el trabajo", "content": "Hoy fue un buen día"} | overrides
    return client.post(BASE, json=body)


def test_create_returns_201_with_contract_shape(client: TestClient) -> None:
    response = _create(client)
    assert response.status_code == 201
    data = response.json()
    assert set(data) == {
        "id",
        "mood_entry_id",
        "title",
        "content",
        "created_at",
        "updated_at",
        "edited_at",
        "deleted_at",
    }
    assert data["mood_entry_id"] is None
    assert data["created_at"] == "2026-09-29T21:00:00Z"


def test_create_linked_to_a_mood_entry(
    client: TestClient, mood_repo: FakeMoodEntryRepository
) -> None:
    mood_id = _mood_entry_id(mood_repo)
    assert _create(client, mood_entry_id=mood_id).json()["mood_entry_id"] == mood_id


def test_unknown_mood_entry_is_a_validation_error(client: TestClient) -> None:
    response = _create(client, mood_entry_id=str(uuid4()))
    assert response.status_code == 422
    error = response.json()["error"]
    assert error["code"] == "VALIDATION_ERROR"
    assert error["details"][0]["field"] == "mood_entry_id"


def test_blank_or_missing_content_is_rejected(client: TestClient) -> None:
    blank = _create(client, content="   ")
    assert blank.status_code == 422
    assert blank.json()["error"]["details"][0]["field"] == "content"

    missing = client.post(BASE, json={"title": "solo titulo"})
    assert missing.status_code == 422
    assert missing.json()["error"]["details"][0]["field"] == "content"


def test_unknown_field_is_rejected(client: TestClient) -> None:
    assert _create(client, mood="GOOD").status_code == 422


def test_list_envelope_default_limit_and_filter(
    client: TestClient, mood_repo: FakeMoodEntryRepository
) -> None:
    mood_id = _mood_entry_id(mood_repo)
    _create(client)
    _create(client, mood_entry_id=mood_id)

    everything = client.get(BASE).json()
    assert everything["total"] == 2 and everything["limit"] == 20 and everything["offset"] == 0

    filtered = client.get(BASE, params={"mood_entry_id": mood_id}).json()
    assert filtered["total"] == 1


def test_list_limit_above_100_is_rejected(client: TestClient) -> None:
    assert client.get(BASE, params={"limit": 101}).status_code == 422


def test_get_unknown_id_returns_404(client: TestClient) -> None:
    response = client.get(f"{BASE}/{uuid4()}")
    assert response.status_code == 404
    assert response.json()["error"]["code"] == "NOT_FOUND"


def test_put_replaces_fields_and_can_unlink(
    client: TestClient, mood_repo: FakeMoodEntryRepository
) -> None:
    created = _create(client, mood_entry_id=_mood_entry_id(mood_repo)).json()
    response = client.put(
        f"{BASE}/{created['id']}", json={"mood_entry_id": None, "content": "Editado"}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["mood_entry_id"] is None and data["title"] is None
    assert data["content"] == "Editado"


def test_delete_returns_204_then_404(client: TestClient) -> None:
    created = _create(client).json()
    assert client.delete(f"{BASE}/{created['id']}").status_code == 204
    assert client.get(f"{BASE}/{created['id']}").status_code == 404
