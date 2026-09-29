import datetime as dt
from dataclasses import dataclass
from uuid import UUID, uuid4

from app.domain.validation import normalize_optional_text
from app.domain.value_objects.mood_type import MoodType

MAX_NOTE_LENGTH = 500


@dataclass(slots=True)
class MoodEntry:
    """Registro emocional de un usuario en un día. Como máximo uno por día."""

    id: UUID
    user_id: UUID
    date: dt.date
    mood: MoodType
    note: str | None
    created_at: dt.datetime
    updated_at: dt.datetime

    @classmethod
    def create(
        cls,
        *,
        user_id: UUID,
        date: dt.date,
        mood: MoodType,
        note: str | None,
        now: dt.datetime,
        id: UUID | None = None,
    ) -> "MoodEntry":
        return cls(
            id=id if id is not None else uuid4(),
            user_id=user_id,
            date=date,
            mood=mood,
            note=normalize_optional_text(note, field="note", max_length=MAX_NOTE_LENGTH),
            created_at=now,
            updated_at=now,
        )

    def update(self, *, mood: MoodType, note: str | None, now: dt.datetime) -> None:
        """Reemplaza `mood` y `note`. La `date` es inmutable."""
        self.mood = mood
        self.note = normalize_optional_text(note, field="note", max_length=MAX_NOTE_LENGTH)
        self.updated_at = now
