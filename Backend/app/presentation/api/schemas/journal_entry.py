import datetime as dt
from uuid import UUID

from pydantic import BaseModel, ConfigDict

from app.domain.entities import JournalEntry


class JournalEntryCreateRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    id: UUID | None = None
    mood_entry_id: UUID | None = None
    icon: str | None = None
    title: str | None = None
    content: str
    created_at: dt.datetime | None = None
    edited_at: dt.datetime | None = None


class JournalEntryUpsertRequest(BaseModel):
    """Body de `PUT /journal-entries/{id}`: crea o actualiza (reemplazo completo)."""

    model_config = ConfigDict(extra="forbid")

    mood_entry_id: UUID | None = None
    icon: str | None = None
    title: str | None = None
    content: str
    created_at: dt.datetime | None = None  # solo se usa al crear
    edited_at: dt.datetime | None = None


def _utc(value: dt.datetime) -> dt.datetime:
    return value.astimezone(dt.UTC)


class JournalEntryResponse(BaseModel):
    id: UUID
    mood_entry_id: UUID | None
    icon: str | None
    title: str | None
    content: str
    created_at: dt.datetime
    updated_at: dt.datetime
    edited_at: dt.datetime
    deleted_at: dt.datetime | None

    @classmethod
    def from_entity(cls, entry: JournalEntry) -> "JournalEntryResponse":
        return cls(
            id=entry.id,
            mood_entry_id=entry.mood_entry_id,
            icon=entry.icon,
            title=entry.title,
            content=entry.content,
            created_at=_utc(entry.created_at),
            updated_at=_utc(entry.updated_at),
            edited_at=_utc(entry.edited_at),
            deleted_at=_utc(entry.deleted_at) if entry.deleted_at else None,
        )


class JournalEntryListResponse(BaseModel):
    items: list[JournalEntryResponse]
    total: int
    limit: int
    offset: int
