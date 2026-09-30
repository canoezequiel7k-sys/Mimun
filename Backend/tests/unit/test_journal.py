import datetime as dt
from uuid import uuid4

import pytest

from app.domain.entities import JournalEntry
from app.domain.errors import DomainValidationError

NOW = dt.datetime(2026, 9, 29, 21, 0, tzinfo=dt.UTC)
LATER = NOW + dt.timedelta(hours=1)


def _create(title: str | None = "Titulo", content: str = "Contenido") -> JournalEntry:
    return JournalEntry.create(
        user_id=uuid4(), mood_entry_id=None, title=title, content=content, now=NOW
    )


def test_create_normalizes_title() -> None:
    assert _create(title="  hola ").title == "hola"
    assert _create(title="  ").title is None


def test_title_limit_is_120() -> None:
    assert _create(title="a" * 120).title is not None
    with pytest.raises(DomainValidationError) as error:
        _create(title="a" * 121)
    assert error.value.field == "title"


def test_content_is_required_and_limited() -> None:
    with pytest.raises(DomainValidationError):
        _create(content="   ")
    assert len(_create(content="a" * 10_000).content) == 10_000
    with pytest.raises(DomainValidationError) as error:
        _create(content="a" * 10_001)
    assert error.value.field == "content"


def test_update_replaces_editable_fields() -> None:
    entry = _create()
    mood_entry_id = uuid4()
    entry.update(mood_entry_id=mood_entry_id, title=None, content=" nuevo ", now=LATER)
    assert entry.mood_entry_id == mood_entry_id
    assert entry.title is None
    assert entry.content == "nuevo"
    assert entry.created_at == NOW
    assert entry.updated_at == LATER
