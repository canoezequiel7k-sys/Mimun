import datetime as dt
from dataclasses import dataclass
from uuid import UUID, uuid4

from app.domain.sync import resolve_client_instant
from app.domain.validation import normalize_optional_text, normalize_required_text

MAX_TITLE_LENGTH = 120
MAX_CONTENT_LENGTH = 10_000


@dataclass(slots=True)
class JournalEntry:
    """Reflexión escrita por el usuario, opcionalmente asociada a un registro emocional."""

    id: UUID
    user_id: UUID
    mood_entry_id: UUID | None
    title: str | None
    content: str
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
        mood_entry_id: UUID | None,
        title: str | None,
        content: str,
        now: dt.datetime,
        id: UUID | None = None,
        created_at: dt.datetime | None = None,
        edited_at: dt.datetime | None = None,
    ) -> "JournalEntry":
        return cls(
            id=id if id is not None else uuid4(),
            user_id=user_id,
            mood_entry_id=mood_entry_id,
            title=normalize_optional_text(title, field="title", max_length=MAX_TITLE_LENGTH),
            content=normalize_required_text(
                content, field="content", max_length=MAX_CONTENT_LENGTH
            ),
            created_at=resolve_client_instant(created_at, now, field="created_at"),
            updated_at=now,
            edited_at=resolve_client_instant(edited_at, now, field="edited_at"),
        )

    def update(
        self,
        *,
        mood_entry_id: UUID | None,
        title: str | None,
        content: str,
        now: dt.datetime,
        edited_at: dt.datetime | None = None,
    ) -> None:
        """Reemplazo completo de los campos editables (y revive la reflexión si estaba borrada).

        Valida antes de asignar: si falla, la entidad queda intacta.
        """
        normalized_title = normalize_optional_text(
            title, field="title", max_length=MAX_TITLE_LENGTH
        )
        normalized_content = normalize_required_text(
            content, field="content", max_length=MAX_CONTENT_LENGTH
        )
        edited = resolve_client_instant(edited_at, now, field="edited_at")
        self.mood_entry_id = mood_entry_id
        self.title = normalized_title
        self.content = normalized_content
        self.updated_at = now
        self.edited_at = edited
        self.deleted_at = None

    def mark_deleted(self, now: dt.datetime) -> None:
        self.deleted_at = now
        self.updated_at = now
        self.edited_at = now
