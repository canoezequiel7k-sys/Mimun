import datetime as dt
from uuid import UUID, uuid4

import pytest

from app.application.use_cases.mood_entries import (
    CreateMoodEntryCommand,
    ListMoodEntriesQuery,
    MoodEntryUseCases,
    UpsertMoodEntryCommand,
)
from app.domain.entities import MoodEntry
from app.domain.errors import DomainValidationError, MoodEntryAlreadyExistsError, NotFoundError
from app.domain.value_objects import MoodType
from tests.fakes import FakeClock, FakeMoodEntryRepository

NOW = dt.datetime(2026, 9, 29, 14, 30, tzinfo=dt.UTC)
HOUR = dt.timedelta(hours=1)
USER = uuid4()


@pytest.fixture
def clock() -> FakeClock:
    return FakeClock(NOW)


@pytest.fixture
def use_cases(clock: FakeClock) -> MoodEntryUseCases:
    return MoodEntryUseCases.build(FakeMoodEntryRepository(), clock)


def _command(
    date: dt.date = dt.date(2026, 9, 29),
    user_id: UUID = USER,
    edited_at: dt.datetime | None = None,
) -> CreateMoodEntryCommand:
    return CreateMoodEntryCommand(
        user_id=user_id, date=date, mood=MoodType.GOOD, note=None, edited_at=edited_at
    )


def _upsert(
    entry_id: UUID,
    mood: MoodType = MoodType.BAD,
    note: str | None = None,
    date: dt.date | None = None,
    edited_at: dt.datetime | None = None,
) -> UpsertMoodEntryCommand:
    return UpsertMoodEntryCommand(
        user_id=USER, entry_id=entry_id, mood=mood, note=note, date=date, edited_at=edited_at
    )


def _list(
    use_cases: MoodEntryUseCases, since: dt.datetime | None = None, date_from: dt.date | None = None
):
    return use_cases.list_entries.execute(
        ListMoodEntriesQuery(
            user_id=USER,
            date_from=date_from,
            date_to=None,
            limit=10,
            offset=0,
            updated_since=since,
        )
    )


def test_create_then_get(use_cases: MoodEntryUseCases) -> None:
    created = use_cases.create.execute(_command())
    assert use_cases.get.execute(USER, created.id) == created


def test_create_allows_tomorrow_but_not_later(use_cases: MoodEntryUseCases) -> None:
    use_cases.create.execute(_command(dt.date(2026, 9, 30)))
    with pytest.raises(DomainValidationError) as error:
        use_cases.create.execute(_command(dt.date(2026, 10, 1)))
    assert error.value.field == "date"


def test_one_active_entry_per_day_per_user(use_cases: MoodEntryUseCases) -> None:
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


def test_upsert_updates_mood_and_note_but_not_the_date(use_cases: MoodEntryUseCases) -> None:
    created = use_cases.create.execute(_command())
    result = use_cases.upsert.execute(_upsert(created.id, mood=MoodType.BAD, note="mal"))
    assert result.created is False
    assert result.entry.mood is MoodType.BAD and result.entry.note == "mal"
    assert result.entry.date == created.date


def test_upsert_creates_with_the_client_id_and_needs_a_date(use_cases: MoodEntryUseCases) -> None:
    client_id = uuid4()
    with pytest.raises(NotFoundError):
        use_cases.upsert.execute(_upsert(client_id))

    result = use_cases.upsert.execute(_upsert(client_id, date=dt.date(2026, 9, 29)))
    assert result.created is True and result.entry.id == client_id


def test_upsert_does_not_reveal_an_id_owned_by_another_user(clock: FakeClock) -> None:
    repository = FakeMoodEntryRepository()
    foreign_entry = MoodEntry.create(
        user_id=uuid4(),
        date=dt.date(2026, 9, 29),
        mood=MoodType.GOOD,
        note=None,
        now=NOW,
    )
    repository.add(foreign_entry)
    use_cases = MoodEntryUseCases.build(repository, clock)

    with pytest.raises(NotFoundError):
        use_cases.upsert.execute(_upsert(foreign_entry.id, date=foreign_entry.date))


def test_upsert_is_idempotent(use_cases: MoodEntryUseCases) -> None:
    client_id = uuid4()
    first = use_cases.upsert.execute(_upsert(client_id, date=dt.date(2026, 9, 29), edited_at=NOW))
    again = use_cases.upsert.execute(_upsert(client_id, date=dt.date(2026, 9, 29), edited_at=NOW))
    assert first.created is True and again.created is False
    assert again.entry.mood is MoodType.BAD
    assert (
        use_cases.list_entries.execute(
            ListMoodEntriesQuery(user_id=USER, date_from=None, date_to=None, limit=10, offset=0)
        ).total
        == 1
    )


def test_upsert_rejects_changing_the_date(use_cases: MoodEntryUseCases) -> None:
    created = use_cases.create.execute(_command())
    with pytest.raises(DomainValidationError) as error:
        use_cases.upsert.execute(_upsert(created.id, date=dt.date(2026, 9, 28)))
    assert error.value.field == "date"


def test_upsert_on_an_occupied_day_with_another_id_is_a_conflict(
    use_cases: MoodEntryUseCases,
) -> None:
    use_cases.create.execute(_command())
    with pytest.raises(MoodEntryAlreadyExistsError):
        use_cases.upsert.execute(_upsert(uuid4(), date=dt.date(2026, 9, 29)))


def test_stale_edit_does_not_overwrite_a_newer_one(
    use_cases: MoodEntryUseCases, clock: FakeClock
) -> None:
    created = use_cases.create.execute(_command())
    clock.advance(HOUR)

    stale = use_cases.upsert.execute(_upsert(created.id, edited_at=NOW - HOUR))
    assert stale.entry.mood is MoodType.GOOD

    newer = use_cases.upsert.execute(_upsert(created.id, mood=MoodType.AWFUL, edited_at=NOW + HOUR))
    assert newer.entry.mood is MoodType.AWFUL
    assert newer.entry.updated_at == NOW + HOUR


def test_client_instants_are_clamped_to_server_time_and_need_a_timezone(
    use_cases: MoodEntryUseCases,
) -> None:
    created = use_cases.create.execute(_command(edited_at=NOW + 10 * HOUR))
    assert created.edited_at == NOW

    with pytest.raises(DomainValidationError) as error:
        use_cases.create.execute(
            _command(dt.date(2026, 9, 28), edited_at=dt.datetime(2026, 9, 29, 10, 0))
        )
    assert error.value.field == "edited_at"


def test_delete_is_soft_idempotent_and_frees_the_day(use_cases: MoodEntryUseCases) -> None:
    created = use_cases.create.execute(_command())
    use_cases.delete.execute(USER, created.id)
    use_cases.delete.execute(USER, created.id)

    with pytest.raises(NotFoundError):
        use_cases.get.execute(USER, created.id)
    assert _list(use_cases).total == 0
    with pytest.raises(NotFoundError):
        use_cases.delete.execute(USER, uuid4())

    use_cases.create.execute(_command())  # el día quedó libre


def test_updated_since_returns_changes_including_deleted(
    use_cases: MoodEntryUseCases, clock: FakeClock
) -> None:
    first = use_cases.create.execute(_command(dt.date(2026, 9, 28)))
    clock.advance(HOUR)
    second = use_cases.create.execute(_command(dt.date(2026, 9, 29)))
    clock.advance(HOUR)
    use_cases.delete.execute(USER, first.id)

    everything = _list(use_cases, since=NOW - HOUR)
    assert [e.id for e in everything.items] == [second.id, first.id]
    assert everything.items[1].deleted_at is not None
    assert [e.id for e in _list(use_cases, since=NOW + HOUR).items] == [first.id]

    with pytest.raises(DomainValidationError):
        _list(use_cases, since=NOW, date_from=dt.date(2026, 9, 1))
    with pytest.raises(DomainValidationError):
        _list(use_cases, since=dt.datetime(2026, 9, 1))


def test_newer_edit_revives_a_deleted_entry_and_older_does_not(
    use_cases: MoodEntryUseCases, clock: FakeClock
) -> None:
    created = use_cases.create.execute(_command())
    clock.advance(HOUR)
    use_cases.delete.execute(USER, created.id)

    stale = use_cases.upsert.execute(_upsert(created.id, edited_at=NOW + dt.timedelta(minutes=30)))
    assert stale.entry.deleted_at is not None

    clock.advance(HOUR)
    revived = use_cases.upsert.execute(
        _upsert(created.id, mood=MoodType.RAD, edited_at=NOW + 2 * HOUR)
    )
    assert revived.entry.deleted_at is None and revived.entry.mood is MoodType.RAD
