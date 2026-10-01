import datetime as dt
from uuid import UUID

from pydantic import BaseModel, ConfigDict

from app.domain.entities import MoodEntry
from app.domain.value_objects import MoodType


class MoodEntryCreateRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    id: UUID | None = None
    date: dt.date
    mood: MoodType
    note: str | None = None
    edited_at: dt.datetime | None = None


class MoodEntryUpsertRequest(BaseModel):
    """Body de `PUT /mood-entries/{id}`: crea o actualiza. `date` solo hace falta al crear."""

    model_config = ConfigDict(extra="forbid")

    mood: MoodType
    note: str | None = None
    date: dt.date | None = None
    edited_at: dt.datetime | None = None


def _utc(value: dt.datetime) -> dt.datetime:
    return value.astimezone(dt.UTC)


class MoodEntryResponse(BaseModel):
    id: UUID
    date: dt.date
    mood: MoodType
    note: str | None
    created_at: dt.datetime
    updated_at: dt.datetime
    edited_at: dt.datetime
    deleted_at: dt.datetime | None

    @classmethod
    def from_entity(cls, entry: MoodEntry) -> "MoodEntryResponse":
        return cls(
            id=entry.id,
            date=entry.date,
            mood=entry.mood,
            note=entry.note,
            created_at=_utc(entry.created_at),
            updated_at=_utc(entry.updated_at),
            edited_at=_utc(entry.edited_at),
            deleted_at=_utc(entry.deleted_at) if entry.deleted_at else None,
        )


class MoodEntryListResponse(BaseModel):
    items: list[MoodEntryResponse]
    total: int
    limit: int
    offset: int
