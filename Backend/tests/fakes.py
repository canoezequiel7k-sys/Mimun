import datetime as dt
from uuid import UUID

from app.domain.entities import MoodEntry
from app.domain.errors import MoodEntryAlreadyExistsError, NotFoundError


class FakeClock:
    def __init__(self, now: dt.datetime) -> None:
        self._now = now

    def now(self) -> dt.datetime:
        return self._now


class FakeMoodEntryRepository:
    def __init__(self) -> None:
        self.items: dict[UUID, MoodEntry] = {}

    def add(self, entry: MoodEntry) -> None:
        if any(e.user_id == entry.user_id and e.date == entry.date for e in self.items.values()):
            raise MoodEntryAlreadyExistsError(entry.date)
        self.items[entry.id] = entry

    def get(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None:
        entry = self.items.get(entry_id)
        return entry if entry and entry.user_id == user_id else None

    def get_by_date(self, user_id: UUID, date: dt.date) -> MoodEntry | None:
        return next((e for e in self._filtered(user_id, date, date)), None)

    def list_entries(
        self,
        user_id: UUID,
        *,
        date_from: dt.date | None,
        date_to: dt.date | None,
        limit: int,
        offset: int,
    ) -> list[MoodEntry]:
        return self._filtered(user_id, date_from, date_to)[offset : offset + limit]

    def count_entries(
        self, user_id: UUID, *, date_from: dt.date | None, date_to: dt.date | None
    ) -> int:
        return len(self._filtered(user_id, date_from, date_to))

    def update(self, entry: MoodEntry) -> None:
        if entry.id not in self.items:
            raise NotFoundError("Registro emocional")
        self.items[entry.id] = entry

    def delete(self, user_id: UUID, entry_id: UUID) -> bool:
        if self.get(user_id, entry_id) is None:
            return False
        del self.items[entry_id]
        return True

    def _filtered(
        self, user_id: UUID, date_from: dt.date | None, date_to: dt.date | None
    ) -> list[MoodEntry]:
        found = [
            e
            for e in self.items.values()
            if e.user_id == user_id
            and (date_from is None or e.date >= date_from)
            and (date_to is None or e.date <= date_to)
        ]
        return sorted(found, key=lambda e: e.date, reverse=True)
