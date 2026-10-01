import datetime as dt
from typing import Protocol
from uuid import UUID

from app.domain.entities import JournalEntry


class JournalEntryRepository(Protocol):
    """Los métodos `get`, `list_entries` y `count_entries` solo ven reflexiones activas.

    `get_any` y `list_changes` incluyen las borradas (borrado lógico, para sincronizar).
    """

    def add(self, entry: JournalEntry) -> None: ...
    def get(self, user_id: UUID, entry_id: UUID) -> JournalEntry | None: ...
    def get_any(self, user_id: UUID, entry_id: UUID) -> JournalEntry | None: ...

    def list_entries(
        self, user_id: UUID, *, mood_entry_id: UUID | None, limit: int, offset: int
    ) -> list[JournalEntry]:
        """Activas, por `created_at` descendente."""
        ...

    def count_entries(self, user_id: UUID, *, mood_entry_id: UUID | None) -> int: ...

    def list_changes(
        self, user_id: UUID, *, updated_since: dt.datetime, limit: int, offset: int
    ) -> list[JournalEntry]:
        """Con `updated_at` > `updated_since`, incluidas borradas, orden `updated_at` ascendente."""
        ...

    def count_changes(self, user_id: UUID, *, updated_since: dt.datetime) -> int: ...

    def update(self, entry: JournalEntry) -> None:
        """Persiste mood_entry_id, title, content, updated_at, edited_at y deleted_at.

        Lanza NotFoundError si no existe.
        """
        ...
