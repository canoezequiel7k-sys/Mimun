import datetime as dt
from collections.abc import Iterator
from uuid import uuid4

import pytest
from fastapi.testclient import TestClient

from app.application.use_cases.journal_entries import JournalEntryUseCases
from app.application.use_cases.mood_entries import MoodEntryUseCases
from app.main import create_app
from app.presentation.api.deps import (
    get_current_user_id,
    get_journal_entry_use_cases,
    get_mood_entry_use_cases,
)
from tests.fakes import FakeClock, FakeJournalEntryRepository, FakeMoodEntryRepository

NOW = dt.datetime(2026, 9, 30, 12, 0, tzinfo=dt.UTC)
USER_ID = uuid4()
MOOD = "/api/v1/mood-entries"
JOURNAL = "/api/v1/journal-entries"


@pytest.fixture
def client() -> Iterator[TestClient]:
    app = create_app()
    mood_repo = FakeMoodEntryRepository()
    moods = MoodEntryUseCases.build(mood_repo, FakeClock(NOW))
    journal = JournalEntryUseCases.build(FakeJournalEntryRepository(), mood_repo, FakeClock(NOW))
    app.dependency_overrides[get_current_user_id] = lambda: USER_ID
    app.dependency_overrides[get_mood_entry_use_cases] = lambda: moods
    app.dependency_overrides[get_journal_entry_use_cases] = lambda: journal
    with TestClient(app) as test_client:
        yield test_client


def _put_mood(client: TestClient, entry_id: str, **body: object):
    payload = {"date": "2026-09-30", "mood": "GOOD", "edited_at": "2026-09-30T10:00:00Z"} | body
    return client.put(f"{MOOD}/{entry_id}", json=payload)


def test_put_creates_with_the_client_id_then_updates(client: TestClient) -> None:
    entry_id = str(uuid4())
    created = _put_mood(client, entry_id)
    assert created.status_code == 201
    assert created.json()["id"] == entry_id
    assert created.json()["edited_at"] == "2026-09-30T10:00:00Z"
    assert created.json()["updated_at"] == "2026-09-30T12:00:00Z"
    assert created.json()["deleted_at"] is None

    updated = _put_mood(client, entry_id, mood="MEH", edited_at="2026-09-30T11:00:00Z")
    assert updated.status_code == 200
    assert updated.json()["mood"] == "MEH"


def test_put_is_idempotent(client: TestClient) -> None:
    entry_id = str(uuid4())
    first = _put_mood(client, entry_id)
    again = _put_mood(client, entry_id)
    assert (first.status_code, again.status_code) == (201, 200)
    assert first.json() == again.json()


def test_put_without_date_on_an_unknown_id_is_404(client: TestClient) -> None:
    response = client.put(f"{MOOD}/{uuid4()}", json={"mood": "GOOD"})
    assert response.status_code == 404
    assert response.json()["error"]["code"] == "NOT_FOUND"


def test_put_on_an_occupied_day_with_another_id_is_409(client: TestClient) -> None:
    _put_mood(client, str(uuid4()))
    response = _put_mood(client, str(uuid4()))
    assert response.status_code == 409
    assert response.json()["error"]["code"] == "MOOD_ENTRY_ALREADY_EXISTS"


def test_stale_put_returns_the_server_version_unchanged(client: TestClient) -> None:
    entry_id = str(uuid4())
    _put_mood(client, entry_id, mood="GOOD", edited_at="2026-09-30T10:00:00Z")
    response = _put_mood(client, entry_id, mood="AWFUL", edited_at="2026-09-30T09:00:00Z")
    assert response.status_code == 200
    assert response.json()["mood"] == "GOOD"


def test_naive_edited_at_is_rejected(client: TestClient) -> None:
    response = _put_mood(client, str(uuid4()), edited_at="2026-09-30T10:00:00")
    assert response.status_code == 422
    assert response.json()["error"]["details"][0]["field"] == "edited_at"


def test_delete_is_idempotent_and_the_tombstone_is_downloadable(client: TestClient) -> None:
    entry_id = str(uuid4())
    _put_mood(client, entry_id)

    assert client.delete(f"{MOOD}/{entry_id}").status_code == 204
    assert client.delete(f"{MOOD}/{entry_id}").status_code == 204
    assert client.get(f"{MOOD}/{entry_id}").status_code == 404
    assert client.get(MOOD).json()["total"] == 0

    pulled = client.get(MOOD, params={"updated_since": "2026-09-30T00:00:00Z"}).json()
    assert pulled["total"] == 1
    assert pulled["items"][0]["id"] == entry_id
    assert pulled["items"][0]["deleted_at"] == "2026-09-30T12:00:00Z"


def test_delete_of_an_unknown_id_is_404(client: TestClient) -> None:
    assert client.delete(f"{MOOD}/{uuid4()}").status_code == 404


def test_updated_since_cannot_be_combined_with_a_range(client: TestClient) -> None:
    response = client.get(
        MOOD, params={"updated_since": "2026-09-30T00:00:00Z", "from": "2026-09-01"}
    )
    assert response.status_code == 422
    assert response.json()["error"]["details"][0]["field"] == "updated_since"


def test_stale_delete_wins_over_an_older_edit_and_a_newer_edit_revives(client: TestClient) -> None:
    entry_id = str(uuid4())
    _put_mood(client, entry_id)
    client.delete(f"{MOOD}/{entry_id}")  # el borrado queda con edited_at = hora del servidor

    stale = _put_mood(client, entry_id, mood="RAD", edited_at="2026-09-30T11:00:00Z")
    assert stale.status_code == 200 and stale.json()["deleted_at"] is not None

    revived = _put_mood(client, entry_id, mood="RAD", edited_at="2026-09-30T12:00:00Z")
    assert revived.status_code == 200
    assert revived.json()["deleted_at"] is None and revived.json()["mood"] == "RAD"


def test_journal_put_creates_with_authoring_time_and_delete_is_soft(client: TestClient) -> None:
    entry_id = str(uuid4())
    created = client.put(
        f"{JOURNAL}/{entry_id}",
        json={"content": "texto", "created_at": "2026-09-29T21:00:00Z"},
    )
    assert created.status_code == 201
    assert created.json()["created_at"] == "2026-09-29T21:00:00Z"
    assert created.json()["updated_at"] == "2026-09-30T12:00:00Z"

    assert client.delete(f"{JOURNAL}/{entry_id}").status_code == 204
    assert client.delete(f"{JOURNAL}/{entry_id}").status_code == 204
    assert client.get(f"{JOURNAL}/{entry_id}").status_code == 404

    pulled = client.get(JOURNAL, params={"updated_since": "2026-09-30T00:00:00Z"}).json()
    assert pulled["items"][0]["deleted_at"] is not None
