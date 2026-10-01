import datetime as dt
from typing import Protocol
from uuid import UUID

from app.domain.entities import MoodEntry


class MoodEntryRepository(Protocol):
    """Los métodos `get`, `list_entries` y `count_entries` solo ven registros activos.

    `get_any` y `list_changes` incluyen los borrados (borrado lógico, para sincronizar).
    """

    def add(self, entry: MoodEntry) -> None:
        """Lanza MoodEntryAlreadyExistsError si ya hay un registro activo para esa fecha."""
        ...

    def get(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None: ...
    def get_any(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None: ...
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
        """Activos, por fecha descendente."""
        ...

    def count_entries(
        self, user_id: UUID, *, date_from: dt.date | None, date_to: dt.date | None
    ) -> int: ...

    def list_changes(
        self, user_id: UUID, *, updated_since: dt.datetime, limit: int, offset: int
    ) -> list[MoodEntry]:
        """Con `updated_at` > `updated_since`, incluidos borrados, orden `updated_at` ascendente."""
        ...

    def count_changes(self, user_id: UUID, *, updated_since: dt.datetime) -> int: ...

    def update(self, entry: MoodEntry) -> None:
        """Persiste mood, note, updated_at, edited_at y deleted_at.

        Lanza NotFoundError si no existe, y MoodEntryAlreadyExistsError si al revivirlo
        su fecha ya la ocupa otro registro activo. Al borrarlo, deja sin asociación
        a sus reflexiones.
        """
        ...
