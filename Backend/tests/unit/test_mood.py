import datetime as dt
from uuid import uuid4

import pytest

from app.domain.entities import MoodEntry
from app.domain.errors import DomainValidationError
from app.domain.value_objects import MoodType

NOW = dt.datetime(2026, 9, 29, 14, 30, tzinfo=dt.UTC)
LATER = NOW + dt.timedelta(hours=1)
TODAY = dt.date(2026, 9, 29)


def _create(note: str | None = "hola", **kwargs) -> MoodEntry:
    return MoodEntry.create(
        user_id=kwargs.pop("user_id", uuid4()),
        date=TODAY,
        mood=MoodType.GOOD,
        note=note,
        now=NOW,
        **kwargs,
    )


def test_mood_type_has_exactly_five_values() -> None:
    assert [m.value for m in MoodType] == ["RAD", "GOOD", "MEH", "BAD", "AWFUL"]


def test_mood_type_rejects_normal() -> None:
    with pytest.raises(ValueError):
        MoodType("NORMAL")


def test_mood_scores_go_from_5_to_1() -> None:
    assert [m.score for m in MoodType] == [5, 4, 3, 2, 1]


def test_create_trims_note_and_turns_blank_into_none() -> None:
    assert _create(note="  hola  ").note == "hola"
    assert _create(note="   ").note is None
    assert _create(note=None).note is None


def test_create_accepts_500_chars_and_rejects_501() -> None:
    assert len(_create(note="a" * 500).note or "") == 500
    with pytest.raises(DomainValidationError) as error:
        _create(note="a" * 501)
    assert error.value.field == "note"


def test_create_generates_id_or_keeps_the_given_one() -> None:
    given = uuid4()
    assert _create(id=given).id == given
    assert _create().id is not None


def test_create_sets_both_timestamps() -> None:
    entry = _create()
    assert entry.created_at == entry.updated_at == NOW


def test_update_replaces_mood_and_note_but_not_date() -> None:
    entry = _create()
    entry.update(mood=MoodType.AWFUL, note=None, now=LATER)
    assert entry.mood is MoodType.AWFUL
    assert entry.note is None
    assert entry.date == TODAY
    assert entry.created_at == NOW
    assert entry.updated_at == LATER
