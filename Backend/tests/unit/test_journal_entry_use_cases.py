import datetime as dt
from uuid import UUID, uuid4

import pytest

from app.application.use_cases.journal_entries import (
    CreateJournalEntryCommand,
    JournalEntryUseCases,
    ListJournalEntriesQuery,
    UpdateJournalEntryCommand,
)
from app.domain.entities import JournalEntry, MoodEntry
from app.domain.errors import DomainValidationError, NotFoundError
from app.domain.value_objects import MoodType
from tests.fakes import FakeClock, FakeJournalEntryRepository, FakeMoodEntryRepository

NOW = dt.datetime(2026, 9, 29, 21, 0, tzinfo=dt.UTC)
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
    use_cases: JournalEntryUseCases, content: str = "texto", mood_entry_id: UUID | None = None
) -> JournalEntry:
    return use_cases.create.execute(
        CreateJournalEntryCommand(
            user_id=USER, mood_entry_id=mood_entry_id, title=None, content=content
        )
    )


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
    clock.advance(dt.timedelta(hours=1))
    second = _create(use_cases, mood_entry_id=mood.id)

    page = use_cases.list_entries.execute(
        ListJournalEntriesQuery(user_id=USER, mood_entry_id=None, limit=10, offset=0)
    )
    assert [e.id for e in page.items] == [second.id, first.id]
    assert page.total == 2

    only = use_cases.list_entries.execute(
        ListJournalEntriesQuery(user_id=USER, mood_entry_id=mood.id, limit=10, offset=0)
    )
    assert [e.id for e in only.items] == [second.id]


def test_update_replaces_fields_and_validates_mood_entry(
    use_cases: JournalEntryUseCases, mood_repo: FakeMoodEntryRepository
) -> None:
    mood = _mood_entry(mood_repo)
    created = _create(use_cases)
    updated = use_cases.update.execute(
        UpdateJournalEntryCommand(
            user_id=USER,
            entry_id=created.id,
            mood_entry_id=mood.id,
            title="Nuevo",
            content="Editado",
        )
    )
    assert updated.mood_entry_id == mood.id and updated.title == "Nuevo"

    with pytest.raises(DomainValidationError):
        use_cases.update.execute(
            UpdateJournalEntryCommand(
                user_id=USER,
                entry_id=created.id,
                mood_entry_id=uuid4(),
                title=None,
                content="x",
            )
        )


def test_update_and_delete_of_unknown_entry_raise(use_cases: JournalEntryUseCases) -> None:
    with pytest.raises(NotFoundError):
        use_cases.update.execute(
            UpdateJournalEntryCommand(
                user_id=USER, entry_id=uuid4(), mood_entry_id=None, title=None, content="x"
            )
        )
    with pytest.raises(NotFoundError):
        use_cases.delete.execute(USER, uuid4())


def test_delete_then_not_found(use_cases: JournalEntryUseCases) -> None:
    created = _create(use_cases)
    use_cases.delete.execute(USER, created.id)
    with pytest.raises(NotFoundError):
        use_cases.get.execute(USER, created.id)
