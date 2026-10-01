import datetime as dt
from dataclasses import replace
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

HOUR = dt.timedelta(hours=1)


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
        now=NOW + hours * HOUR,
    )


def test_user_lookup_by_email_ignores_case(session: Session, user: User) -> None:
    found = SqlAlchemyUserRepository(session).get_by_email("ANA@Example.com")
    assert found is not None and found.id == user.id


def test_mood_add_and_get(session: Session, user: User) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    entry = _mood(user, 29)
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

    everything = repo.list_entries(user.id, date_from=None, date_to=None, limit=10, offset=0)
    assert [e.date.day for e in everything] == [28, 27, 26, 25]

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
    assert repo.get_any(other.id, entry.id) is None
    assert repo.count_entries(other.id, date_from=None, date_to=None) == 0
    assert repo.count_changes(other.id, updated_since=NOW - HOUR) == 0


def test_mood_update_persists_all_fields(session: Session, user: User) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    entry = _mood(user, 29)
    repo.add(entry)

    entry.update(mood=MoodType.AWFUL, note="mal día", now=NOW + HOUR, edited_at=NOW + HOUR)
    repo.update(entry)
    assert repo.get(user.id, entry.id) == entry


def test_mood_update_of_missing_entry_raises(session: Session, user: User) -> None:
    with pytest.raises(NotFoundError):
        SqlAlchemyMoodEntryRepository(session).update(_mood(user, 29))


def test_soft_deleted_mood_is_hidden_but_visible_in_changes_and_frees_the_day(
    session: Session, user: User
) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    entry = _mood(user, 29)
    repo.add(entry)

    entry.mark_deleted(NOW + HOUR)
    repo.update(entry)

    assert repo.get(user.id, entry.id) is None
    assert repo.get_any(user.id, entry.id) == entry
    assert repo.count_entries(user.id, date_from=None, date_to=None) == 0
    changes = repo.list_changes(user.id, updated_since=NOW, limit=10, offset=0)
    assert [e.id for e in changes] == [entry.id] and changes[0].deleted_at is not None
    assert repo.count_changes(user.id, updated_since=NOW) == 1

    repo.add(_mood(user, 29))  # el índice único parcial ignora los borrados


def test_reviving_a_deleted_mood_on_an_occupied_day_is_a_conflict(
    session: Session, user: User
) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    old = _mood(user, 29)
    repo.add(old)
    old.mark_deleted(NOW + HOUR)
    repo.update(old)
    repo.add(_mood(user, 29))

    old.update(mood=MoodType.RAD, note=None, now=NOW + 2 * HOUR)
    with pytest.raises(MoodEntryAlreadyExistsError):
        repo.update(old)


def test_changes_are_ordered_by_updated_at_ascending(session: Session, user: User) -> None:
    repo = SqlAlchemyMoodEntryRepository(session)
    later = MoodEntry.create(
        user_id=user.id, date=dt.date(2026, 9, 27), mood=MoodType.GOOD, note=None, now=NOW + HOUR
    )
    earlier = _mood(user, 28)
    repo.add(later)
    repo.add(earlier)
    changes = repo.list_changes(user.id, updated_since=NOW - HOUR, limit=10, offset=0)
    assert [e.id for e in changes] == [earlier.id, later.id]
    assert repo.list_changes(user.id, updated_since=NOW, limit=10, offset=0) == [later]


def test_journal_crud_and_filters(session: Session, user: User) -> None:
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


def test_journal_repository_isolates_users(session: Session, user: User) -> None:
    other = make_user(session, "beto@example.com")
    journals = SqlAlchemyJournalEntryRepository(session)
    entry = _journal(user)
    journals.add(entry)

    assert journals.get(other.id, entry.id) is None
    assert journals.get_any(other.id, entry.id) is None
    assert journals.list_entries(other.id, mood_entry_id=None, limit=10, offset=0) == []
    assert journals.count_entries(other.id, mood_entry_id=None) == 0
    assert journals.list_changes(other.id, updated_since=NOW - HOUR, limit=10, offset=0) == []
    assert journals.count_changes(other.id, updated_since=NOW - HOUR) == 0

    with pytest.raises(NotFoundError):
        journals.update(replace(entry, user_id=other.id, content="acceso no autorizado"))
    assert journals.get(user.id, entry.id) == entry


def test_soft_deleting_a_mood_entry_detaches_its_journal_entries(
    session: Session, user: User
) -> None:
    moods = SqlAlchemyMoodEntryRepository(session)
    journals = SqlAlchemyJournalEntryRepository(session)
    mood = _mood(user, 29)
    moods.add(mood)
    linked = _journal(user, mood_entry_id=mood.id)
    journals.add(linked)

    mood.mark_deleted(NOW + HOUR)
    moods.update(mood)

    after = journals.get(user.id, linked.id)
    assert after is not None
    assert after.mood_entry_id is None
    assert after.updated_at == NOW + HOUR  # cambió: el cliente lo descarga en el próximo pull


def test_soft_deleted_journal_is_hidden_but_visible_in_changes(
    session: Session, user: User
) -> None:
    journals = SqlAlchemyJournalEntryRepository(session)
    entry = _journal(user)
    journals.add(entry)

    entry.mark_deleted(NOW + HOUR)
    journals.update(entry)

    assert journals.get(user.id, entry.id) is None
    assert journals.count_entries(user.id, mood_entry_id=None) == 0
    changes = journals.list_changes(user.id, updated_since=NOW, limit=10, offset=0)
    assert [e.id for e in changes] == [entry.id] and changes[0].deleted_at is not None


def test_journal_with_unknown_mood_entry_raises_validation_error(
    session: Session, user: User
) -> None:
    with pytest.raises(DomainValidationError) as error:
        SqlAlchemyJournalEntryRepository(session).add(_journal(user, mood_entry_id=uuid4()))
    assert error.value.field == "mood_entry_id"
