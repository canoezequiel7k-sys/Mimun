import datetime as dt
from typing import Protocol
from uuid import UUID

from app.domain.entities import MoodEntry


class MoodEntryRepository(Protocol):
    def add(self, entry: MoodEntry) -> None:
        """Lanza MoodEntryAlreadyExistsError si ya hay un registro para esa fecha."""
        ...

    def get(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None: ...
    def get_by_date(self, user_id: UUID, date: dt.date) -> MoodEntry | None: ...

    def list_entries(
        self,
        user_id: UUID,
        *,
        date_from: dt.date | None,
        date_to: dt.date | None,
        limit: int,
        offset: int,
    ) -> list[MoodEntry]:
        """Ordenados por fecha descendente."""
        ...

    def count_entries(
        self, user_id: UUID, *, date_from: dt.date | None, date_to: dt.date | None
    ) -> int: ...

    def update(self, entry: MoodEntry) -> None:
        """Lanza NotFoundError si no existe o es de otro usuario."""
        ...

    def delete(self, user_id: UUID, entry_id: UUID) -> bool: ...
