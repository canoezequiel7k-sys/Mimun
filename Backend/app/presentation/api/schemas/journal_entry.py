import datetime as dt
from uuid import UUID

from pydantic import BaseModel, ConfigDict

from app.domain.entities import JournalEntry


class JournalEntryCreateRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    id: UUID | None = None
    mood_entry_id: UUID | None = None
    title: str | None = None
    content: str


class JournalEntryUpdateRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    mood_entry_id: UUID | None = None
    title: str | None = None
    content: str


class JournalEntryResponse(BaseModel):
    id: UUID
    mood_entry_id: UUID | None
    title: str | None
    content: str
    created_at: dt.datetime
    updated_at: dt.datetime

    @classmethod
    def from_entity(cls, entry: JournalEntry) -> "JournalEntryResponse":
        return cls(
            id=entry.id,
            mood_entry_id=entry.mood_entry_id,
            title=entry.title,
            content=entry.content,
            created_at=entry.created_at.astimezone(dt.UTC),
            updated_at=entry.updated_at.astimezone(dt.UTC),
        )


class JournalEntryListResponse(BaseModel):
    items: list[JournalEntryResponse]
    total: int
    limit: int
    offset: int
