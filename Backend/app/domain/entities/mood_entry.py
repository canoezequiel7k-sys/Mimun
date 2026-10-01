import datetime as dt
from dataclasses import dataclass
from uuid import UUID, uuid4

from app.domain.sync import resolve_client_instant
from app.domain.validation import normalize_optional_text
from app.domain.value_objects.mood_type import MoodType

MAX_NOTE_LENGTH = 500


@dataclass(slots=True)
class MoodEntry:
    """Registro emocional de un usuario en un día. Como máximo uno activo por día."""

    id: UUID
    user_id: UUID
    date: dt.date
    mood: MoodType
    note: str | None
    created_at: dt.datetime
    updated_at: dt.datetime  # lo asigna el servidor: cursor de descarga
    edited_at: dt.datetime  # lo informa el cliente: resuelve conflictos
    deleted_at: dt.datetime | None = None

    @property
    def is_deleted(self) -> bool:
        return self.deleted_at is not None

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
        edited_at: dt.datetime | None = None,
    ) -> "MoodEntry":
        return cls(
            id=id if id is not None else uuid4(),
            user_id=user_id,
            date=date,
            mood=mood,
            note=normalize_optional_text(note, field="note", max_length=MAX_NOTE_LENGTH),
            created_at=now,
            updated_at=now,
            edited_at=resolve_client_instant(edited_at, now, field="edited_at"),
        )

    def update(
        self,
        *,
        mood: MoodType,
        note: str | None,
        now: dt.datetime,
        edited_at: dt.datetime | None = None,
    ) -> None:
        """Reemplaza `mood` y `note` (y revive el registro si estaba borrado).

        La `date` es inmutable. Valida antes de asignar: si falla, la entidad queda intacta.
        """
        normalized_note = normalize_optional_text(note, field="note", max_length=MAX_NOTE_LENGTH)
        edited = resolve_client_instant(edited_at, now, field="edited_at")
        self.mood = mood
        self.note = normalized_note
        self.updated_at = now
        self.edited_at = edited
        self.deleted_at = None

    def mark_deleted(self, now: dt.datetime) -> None:
        self.deleted_at = now
        self.updated_at = now
        self.edited_at = now
