import datetime as dt
from uuid import UUID, uuid4

import pytest

from app.application.use_cases.journal_entries import (
    CreateJournalEntryCommand,
    JournalEntryUseCases,
    ListJournalEntriesQuery,
    UpsertJournalEntryCommand,
)
from app.domain.entities import JournalEntry, MoodEntry
from app.domain.errors import DomainValidationError, NotFoundError
from app.domain.value_objects import MoodType
from tests.fakes import FakeClock, FakeJournalEntryRepository, FakeMoodEntryRepository

NOW = dt.datetime(2026, 9, 29, 21, 0, tzinfo=dt.UTC)
HOUR = dt.timedelta(hours=1)
USER = uuid4()


@pytest.fixture
def clock() -> FakeClock:
    return FakeClock(NOW)


@pytest.fixture
def mood_repo() -> FakeMoodEntryRepository:
    return FakeMoodEntryRepository()


@pytest.fixture
def use_cases(mood_repo: FakeMoodEntryRepository, clock: FakeClock) -> JournalEntryUseCases:
    return JournalEntryUseCases.build(FakeJournalEntryRepository(), mood_repo, clock)


def _mood_entry(repo: FakeMoodEntryRepository, user_id: UUID = USER) -> MoodEntry:
    entry = MoodEntry.create(
        user_id=user_id, date=dt.date(2026, 9, 29), mood=MoodType.GOOD, note=None, now=NOW
    )
    repo.add(entry)
    return entry


def _create(
    use_cases: JournalEntryUseCases,
    content: str = "texto",
    mood_entry_id: UUID | None = None,
    icon: str | None = None,
) -> JournalEntry:
    return use_cases.create.execute(
        CreateJournalEntryCommand(
            user_id=USER, mood_entry_id=mood_entry_id, title=None, content=content
            , icon=icon
        )
    )


def _upsert(
    entry_id: UUID,
    content: str = "editado",
    mood_entry_id: UUID | None = None,
    created_at: dt.datetime | None = None,
    edited_at: dt.datetime | None = None,
    icon: str | None = None,
) -> UpsertJournalEntryCommand:
    return UpsertJournalEntryCommand(
        user_id=USER,
        entry_id=entry_id,
        mood_entry_id=mood_entry_id,
        title=None,
        content=content,
        icon=icon,
        created_at=created_at,
        edited_at=edited_at,
    )


def _list(use_cases: JournalEntryUseCases, **kwargs):
    query = {"mood_entry_id": None, "limit": 10, "offset": 0} | kwargs
    return use_cases.list_entries.execute(ListJournalEntriesQuery(user_id=USER, **query))


def test_create_without_mood_entry(use_cases: JournalEntryUseCases) -> None:
    created = _create(use_cases)
    assert use_cases.get.execute(USER, created.id) == created


def test_create_with_existing_mood_entry(
    use_cases: JournalEntryUseCases, mood_repo: FakeMoodEntryRepository
) -> None:
    mood = _mood_entry(mood_repo)
    assert _create(use_cases, mood_entry_id=mood.id).mood_entry_id == mood.id


def test_create_with_unknown_or_foreign_mood_entry_fails(
    use_cases: JournalEntryUseCases, mood_repo: FakeMoodEntryRepository
) -> None:
    foreign = _mood_entry(mood_repo, user_id=uuid4())
    for mood_entry_id in (uuid4(), foreign.id):
        with pytest.raises(DomainValidationError) as error:
            _create(use_cases, mood_entry_id=mood_entry_id)
        assert error.value.field == "mood_entry_id"


def test_blank_content_is_rejected(use_cases: JournalEntryUseCases) -> None:
    with pytest.raises(DomainValidationError) as error:
        _create(use_cases, content="   ")
    assert error.value.field == "content"


def test_other_users_entry_is_not_found(use_cases: JournalEntryUseCases) -> None:
    created = _create(use_cases)
    with pytest.raises(NotFoundError):
        use_cases.get.execute(uuid4(), created.id)


def test_list_is_newest_first_and_filters_by_mood_entry(
    use_cases: JournalEntryUseCases, mood_repo: FakeMoodEntryRepository, clock: FakeClock
) -> None:
    mood = _mood_entry(mood_repo)
    first = _create(use_cases)
    clock.advance(HOUR)
    second = _create(use_cases, mood_entry_id=mood.id)

    page = _list(use_cases)
    assert [e.id for e in page.items] == [second.id, first.id]
    assert page.total == 2
    assert [e.id for e in _list(use_cases, mood_entry_id=mood.id).items] == [second.id]


def test_upsert_creates_with_client_id_and_authoring_time(use_cases: JournalEntryUseCases) -> None:
    client_id = uuid4()
    authored = NOW - 5 * HOUR
    result = use_cases.upsert.execute(_upsert(client_id, created_at=authored))
    assert result.created is True
    assert result.entry.id == client_id and result.entry.created_at == authored
    assert result.entry.updated_at == NOW


def test_upsert_persists_icon_on_create_and_update(use_cases: JournalEntryUseCases) -> None:
    entry_id = uuid4()
    created = use_cases.upsert.execute(_upsert(entry_id, icon="sun"))
    assert created.entry.icon == "sun"

    updated = use_cases.upsert.execute(_upsert(entry_id, icon="great_v2"))
    assert updated.entry.icon == "great_v2"


def test_upsert_updates_and_validates_the_mood_entry(
    use_cases: JournalEntryUseCases, mood_repo: FakeMoodEntryRepository
) -> None:
    mood = _mood_entry(mood_repo)
    created = _create(use_cases)

    result = use_cases.upsert.execute(_upsert(created.id, mood_entry_id=mood.id))
    assert result.created is False
    assert result.entry.mood_entry_id == mood.id and result.entry.content == "editado"

    with pytest.raises(DomainValidationError):
        use_cases.upsert.execute(_upsert(created.id, mood_entry_id=uuid4()))


def test_stale_edit_does_not_overwrite_a_newer_one(
    use_cases: JournalEntryUseCases, clock: FakeClock
) -> None:
    created = _create(use_cases)
    clock.advance(HOUR)
    stale = use_cases.upsert.execute(_upsert(created.id, content="viejo", edited_at=NOW - HOUR))
    assert stale.entry.content == "texto"

    newer = use_cases.upsert.execute(_upsert(created.id, content="nuevo", edited_at=NOW + HOUR))
    assert newer.entry.content == "nuevo"


def test_delete_is_soft_idempotent_and_shows_up_in_changes(
    use_cases: JournalEntryUseCases, clock: FakeClock
) -> None:
    created = _create(use_cases)
    clock.advance(HOUR)
    use_cases.delete.execute(USER, created.id)
    use_cases.delete.execute(USER, created.id)

    with pytest.raises(NotFoundError):
        use_cases.get.execute(USER, created.id)
    assert _list(use_cases).total == 0

    changes = _list(use_cases, updated_since=NOW)
    assert [e.id for e in changes.items] == [created.id]
    assert changes.items[0].deleted_at is not None

    with pytest.raises(NotFoundError):
        use_cases.delete.execute(USER, uuid4())
    with pytest.raises(DomainValidationError):
        _list(use_cases, updated_since=NOW, mood_entry_id=uuid4())


def test_newer_edit_revives_a_deleted_entry(
    use_cases: JournalEntryUseCases, clock: FakeClock
) -> None:
    created = _create(use_cases)
    clock.advance(HOUR)
    use_cases.delete.execute(USER, created.id)
    clock.advance(HOUR)
    revived = use_cases.upsert.execute(_upsert(created.id, edited_at=NOW + 2 * HOUR))
    assert revived.entry.deleted_at is None
