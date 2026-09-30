"""Fakes en memoria para los tests de `application` y de la API (sin base de datos).

Los fakes de autenticación viven en `tests/fakes_auth.py`.
"""

import datetime as dt
from uuid import UUID

from app.domain.entities import JournalEntry, MoodEntry
from app.domain.errors import MoodEntryAlreadyExistsError, NotFoundError


class FakeClock:
    """Reloj controlable: `now()` devuelve un instante fijo que se puede adelantar."""

    def __init__(self, now: dt.datetime) -> None:
        self._now = now

    def now(self) -> dt.datetime:
        return self._now

    def advance(self, delta: dt.timedelta) -> None:
        self._now += delta


class FakeMoodEntryRepository:
    def __init__(self) -> None:
        self.items: dict[UUID, MoodEntry] = {}

    def add(self, entry: MoodEntry) -> None:
        if self.get_by_date(entry.user_id, entry.date) is not None:
            raise MoodEntryAlreadyExistsError(entry.date)
        self.items[entry.id] = entry

    def get(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None:
        entry = self.items.get(entry_id)
        return entry if entry is not None and entry.user_id == user_id else None

    def get_by_date(self, user_id: UUID, date: dt.date) -> MoodEntry | None:
        return next(
            (e for e in self.items.values() if e.user_id == user_id and e.date == date), None
        )

    def _filtered(
        self, user_id: UUID, date_from: dt.date | None, date_to: dt.date | None
    ) -> list[MoodEntry]:
        return [
            e
            for e in self.items.values()
            if e.user_id == user_id
            and (date_from is None or e.date >= date_from)
            and (date_to is None or e.date <= date_to)
        ]

    def list_entries(
        self,
        user_id: UUID,
        *,
        date_from: dt.date | None,
        date_to: dt.date | None,
        limit: int,
        offset: int,
    ) -> list[MoodEntry]:
        entries = sorted(
            self._filtered(user_id, date_from, date_to), key=lambda e: e.date, reverse=True
        )
        return entries[offset : offset + limit]

    def count_entries(
        self, user_id: UUID, *, date_from: dt.date | None, date_to: dt.date | None
    ) -> int:
        return len(self._filtered(user_id, date_from, date_to))

    def update(self, entry: MoodEntry) -> None:
        if self.get(entry.user_id, entry.id) is None:
            raise NotFoundError("Registro emocional")
        self.items[entry.id] = entry

    def delete(self, user_id: UUID, entry_id: UUID) -> bool:
        if self.get(user_id, entry_id) is None:
            return False
        del self.items[entry_id]
        return True


class FakeJournalEntryRepository:
    def __init__(self) -> None:
        self.items: dict[UUID, JournalEntry] = {}

    def add(self, entry: JournalEntry) -> None:
        self.items[entry.id] = entry

    def get(self, user_id: UUID, entry_id: UUID) -> JournalEntry | None:
        entry = self.items.get(entry_id)
        return entry if entry is not None and entry.user_id == user_id else None

    def _filtered(self, user_id: UUID, mood_entry_id: UUID | None) -> list[JournalEntry]:
        return [
            e
            for e in self.items.values()
            if e.user_id == user_id and (mood_entry_id is None or e.mood_entry_id == mood_entry_id)
        ]

    def list_entries(
        self,
        user_id: UUID,
        *,
        mood_entry_id: UUID | None,
        limit: int,
        offset: int,
    ) -> list[JournalEntry]:
        entries = sorted(
            self._filtered(user_id, mood_entry_id), key=lambda e: e.created_at, reverse=True
        )
        return entries[offset : offset + limit]

    def count_entries(self, user_id: UUID, *, mood_entry_id: UUID | None) -> int:
        return len(self._filtered(user_id, mood_entry_id))

    def update(self, entry: JournalEntry) -> None:
        if self.get(entry.user_id, entry.id) is None:
            raise NotFoundError("Reflexión")
        self.items[entry.id] = entry

    def delete(self, user_id: UUID, entry_id: UUID) -> bool:
        if self.get(user_id, entry_id) is None:
            return False
        del self.items[entry_id]
        return True
