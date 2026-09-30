import datetime as dt
from uuid import UUID, uuid4

import pytest

from app.application.use_cases.mood_entries import (
    CreateMoodEntryCommand,
    ListMoodEntriesQuery,
    MoodEntryUseCases,
    UpdateMoodEntryCommand,
)
from app.domain.errors import DomainValidationError, MoodEntryAlreadyExistsError, NotFoundError
from app.domain.value_objects import MoodType
from tests.fakes import FakeClock, FakeMoodEntryRepository

NOW = dt.datetime(2026, 9, 29, 14, 30, tzinfo=dt.UTC)
USER = uuid4()


@pytest.fixture
def use_cases() -> MoodEntryUseCases:
    return MoodEntryUseCases.build(FakeMoodEntryRepository(), FakeClock(NOW))


def _command(date: dt.date = dt.date(2026, 9, 29), user_id: UUID = USER) -> CreateMoodEntryCommand:
    return CreateMoodEntryCommand(user_id=user_id, date=date, mood=MoodType.GOOD, note=None)


def test_create_then_get(use_cases: MoodEntryUseCases) -> None:
    created = use_cases.create.execute(_command())
    assert use_cases.get.execute(USER, created.id) == created


def test_create_allows_tomorrow_but_not_later(use_cases: MoodEntryUseCases) -> None:
    use_cases.create.execute(_command(dt.date(2026, 9, 30)))
    with pytest.raises(DomainValidationError) as error:
        use_cases.create.execute(_command(dt.date(2026, 10, 1)))
    assert error.value.field == "date"


def test_one_entry_per_day_per_user(use_cases: MoodEntryUseCases) -> None:
    use_cases.create.execute(_command())
    with pytest.raises(MoodEntryAlreadyExistsError):
        use_cases.create.execute(_command())
    use_cases.create.execute(_command(user_id=uuid4()))


def test_other_users_entry_is_not_found(use_cases: MoodEntryUseCases) -> None:
    created = use_cases.create.execute(_command())
    with pytest.raises(NotFoundError):
        use_cases.get.execute(uuid4(), created.id)


def test_list_returns_page_and_validates_range(use_cases: MoodEntryUseCases) -> None:
    for day in (27, 28, 29):
        use_cases.create.execute(_command(dt.date(2026, 9, day)))

    page = use_cases.list_entries.execute(
        ListMoodEntriesQuery(user_id=USER, date_from=None, date_to=None, limit=2, offset=0)
    )
    assert [e.date.day for e in page.items] == [29, 28]
    assert page.total == 3

    with pytest.raises(DomainValidationError):
        use_cases.list_entries.execute(
            ListMoodEntriesQuery(
                user_id=USER,
                date_from=dt.date(2026, 9, 29),
                date_to=dt.date(2026, 9, 1),
                limit=10,
                offset=0,
            )
        )


def test_update_changes_mood_and_note(use_cases: MoodEntryUseCases) -> None:
    created = use_cases.create.execute(_command())
    updated = use_cases.update.execute(
        UpdateMoodEntryCommand(user_id=USER, entry_id=created.id, mood=MoodType.BAD, note="mal")
    )
    assert updated.mood is MoodType.BAD and updated.note == "mal"
    assert updated.date == created.date


def test_update_of_unknown_entry_raises(use_cases: MoodEntryUseCases) -> None:
    with pytest.raises(NotFoundError):
        use_cases.update.execute(
            UpdateMoodEntryCommand(user_id=USER, entry_id=uuid4(), mood=MoodType.BAD, note=None)
        )


def test_delete_then_not_found(use_cases: MoodEntryUseCases) -> None:
    created = use_cases.create.execute(_command())
    use_cases.delete.execute(USER, created.id)
    with pytest.raises(NotFoundError):
        use_cases.delete.execute(USER, created.id)
