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


class MoodEntryUpdateRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    mood: MoodType
    note: str | None = None


class MoodEntryResponse(BaseModel):
    id: UUID
    date: dt.date
    mood: MoodType
    note: str | None
    created_at: dt.datetime
    updated_at: dt.datetime

    @classmethod
    def from_entity(cls, entry: MoodEntry) -> "MoodEntryResponse":
        return cls(
            id=entry.id,
            date=entry.date,
            mood=entry.mood,
            note=entry.note,
            created_at=entry.created_at.astimezone(dt.UTC),
            updated_at=entry.updated_at.astimezone(dt.UTC),
        )


class MoodEntryListResponse(BaseModel):
    items: list[MoodEntryResponse]
    total: int
    limit: int
    offset: int
