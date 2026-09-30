import datetime as dt
from uuid import uuid4

import pytest
from sqlalchemy.orm import Session

from app.domain.entities import JournalEntry, MoodEntry, User
from app.domain.errors import DomainValidationError, MoodEntryAlreadyExistsError, NotFoundError
from app.domain.value_objects import MoodType
from app.infrastructure.repositories.journal_entry_repository import (
    SqlAlchemyJournalEntryRepository,
)
from app.infrastructure.repositories.mood_entry_repository import SqlAlchemyMoodEntryRepository
from app.infrastructure.repositories.user_repository import SqlAlchemyUserRepository
from tests.integration.conftest import NOW, make_user

pytestmark = pytest.mark.integration


def _mood(
    user: User, day: int, mood: MoodType = MoodType.GOOD, note: str | None = None, **kwargs
) -> MoodEntry:
    return MoodEntry.create(
        user_id=user.id, date=dt.date(2026, 9, day), mood=mood, note=note, now=NOW, **kwargs
    )


def _journal(user: User, hours: int = 0, mood_entry_id=None) -> JournalEntry:
    return JournalEntry.create(
        user_id=user.id,
        mood_entry_id=mood_entry_id,
        title=None,
        content="texto",
        now=NOW + dt.timedelta(hours=hours),
    )


def test_user_lookup_by_email_ignores_case(session: Session, user: User) -> None:
    found = SqlAlchemyUserRepository(session).get_by_email("ANA@Example.com")
    assert found is not None and found.id == user.id


def test_mood_add_and_get(session: Session, user: User) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    entry = _mood(user, 29, note=None)
    repo.add(entry)
    assert repo.get(user.id, entry.id) == entry
    assert repo.get_by_date(user.id, entry.date) == entry


def test_mood_duplicate_date_raises(session: Session, user: User) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    repo.add(_mood(user, 29))
    with pytest.raises(MoodEntryAlreadyExistsError):
        repo.add(_mood(user, 29))


def test_mood_duplicate_id_raises_validation_error(session: Session, user: User) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    first = _mood(user, 28)
    repo.add(first)
    with pytest.raises(DomainValidationError) as error:
        repo.add(_mood(user, 29, id=first.id))
    assert error.value.field == "id"


def test_mood_list_orders_filters_and_counts(session: Session, user: User) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    for day in (25, 26, 27, 28):
        repo.add(_mood(user, day))

    dates = [
        e.date.day
        for e in repo.list_entries(user.id, date_from=None, date_to=None, limit=10, offset=0)
    ]
    assert dates == [28, 27, 26, 25]

    ranged = repo.list_entries(
        user.id, date_from=dt.date(2026, 9, 26), date_to=dt.date(2026, 9, 27), limit=10, offset=0
    )
    assert [e.date.day for e in ranged] == [27, 26]
    assert repo.count_entries(user.id, date_from=dt.date(2026, 9, 27), date_to=None) == 2

    page = repo.list_entries(user.id, date_from=None, date_to=None, limit=2, offset=1)
    assert [e.date.day for e in page] == [27, 26]


def test_mood_is_isolated_between_users(session: Session, user: User) -> None:
    other = make_user(session, "beto@example.com")
    repo = SqlAlchemyMoodEntryRepository(session)
    entry = _mood(user, 29)
    repo.add(entry)
    assert repo.get(other.id, entry.id) is None
    assert repo.delete(other.id, entry.id) is False
    assert repo.count_entries(other.id, date_from=None, date_to=None) == 0


def test_mood_update_and_delete(session: Session, user: User) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    entry = _mood(user, 29)
    repo.add(entry)

    entry.update(mood=MoodType.AWFUL, note="mal día", now=NOW + dt.timedelta(hours=1))
    repo.update(entry)
    saved = repo.get(user.id, entry.id)
    assert saved is not None and saved.mood is MoodType.AWFUL and saved.note == "mal día"

    assert repo.delete(user.id, entry.id) is True
    assert repo.delete(user.id, entry.id) is False


def test_mood_update_of_missing_entry_raises(session: Session, user: User) -> None:
    with pytest.raises(NotFoundError):
        SqlAlchemyMoodEntryRepository(session).update(_mood(user, 29))


def test_journal_crud_filter_and_set_null_on_mood_delete(session: Session, user: User) -> None:
    moods = SqlAlchemyMoodEntryRepository(session)
    journals = SqlAlchemyJournalEntryRepository(session)
    mood = _mood(user, 29)
    moods.add(mood)

    linked = _journal(user, hours=1, mood_entry_id=mood.id)
    free = _journal(user, hours=2)
    journals.add(linked)
    journals.add(free)

    listed = journals.list_entries(user.id, mood_entry_id=None, limit=10, offset=0)
    assert [e.id for e in listed] == [free.id, linked.id]
    only_linked = journals.list_entries(user.id, mood_entry_id=mood.id, limit=10, offset=0)
    assert [e.id for e in only_linked] == [linked.id]
    assert journals.count_entries(user.id, mood_entry_id=None) == 2

    moods.delete(user.id, mood.id)
    after = journals.get(user.id, linked.id)
    assert after is not None and after.mood_entry_id is None


def test_journal_with_unknown_mood_entry_raises_validation_error(
    session: Session, user: User
) -> None:
    with pytest.raises(DomainValidationError) as error:
        SqlAlchemyJournalEntryRepository(session).add(_journal(user, mood_entry_id=uuid4()))
    assert error.value.field == "mood_entry_id"
