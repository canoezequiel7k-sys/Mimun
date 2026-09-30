from typing import Protocol
from uuid import UUID

from app.domain.entities import JournalEntry


class JournalEntryRepository(Protocol):
    def add(self, entry: JournalEntry) -> None: ...
    def get(self, user_id: UUID, entry_id: UUID) -> JournalEntry | None: ...

    def list_entries(
        self,
        user_id: UUID,
        *,
        mood_entry_id: UUID | None,
        limit: int,
        offset: int,
    ) -> list[JournalEntry]:
        """Ordenadas por created_at descendente."""
        ...

    def count_entries(self, user_id: UUID, *, mood_entry_id: UUID | None) -> int: ...

    def update(self, entry: JournalEntry) -> None:
        """Lanza NotFoundError si no existe o es de otro usuario."""
        ...

    def delete(self, user_id: UUID, entry_id: UUID) -> bool: ...
