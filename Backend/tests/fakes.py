import datetime as dt
from uuid import UUID

from app.domain.entities import JournalEntry, MoodEntry
from app.domain.errors import (
    DomainValidationError,
    MoodEntryAlreadyExistsError,
    NotFoundError,
)


class FakeClock:
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
        if entry.id in self.items:
            raise DomainValidationError("id", "Ya existe un registro con ese id")
        self._ensure_day_is_free(entry)
        self.items[entry.id] = entry

    def get(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None:
        entry = self.get_any(user_id, entry_id)
        return entry if entry and not entry.is_deleted else None

    def get_any(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None:
        entry = self.items.get(entry_id)
        return entry if entry and entry.user_id == user_id else None

    def get_by_date(self, user_id: UUID, date: dt.date) -> MoodEntry | None:
        return next(iter(self._active(user_id, date, date)), None)

    def list_entries(
        self,
        user_id: UUID,
        *,
        date_from: dt.date | None,
        date_to: dt.date | None,
        limit: int,
        offset: int,
    ) -> list[MoodEntry]:
        return self._active(user_id, date_from, date_to)[offset : offset + limit]

    def count_entries(
        self, user_id: UUID, *, date_from: dt.date | None, date_to: dt.date | None
    ) -> int:
        return len(self._active(user_id, date_from, date_to))

    def list_changes(
        self, user_id: UUID, *, updated_since: dt.datetime, limit: int, offset: int
    ) -> list[MoodEntry]:
        return self._changes(user_id, updated_since)[offset : offset + limit]

    def count_changes(self, user_id: UUID, *, updated_since: dt.datetime) -> int:
        return len(self._changes(user_id, updated_since))

    def update(self, entry: MoodEntry) -> None:
        if entry.id not in self.items:
            raise NotFoundError("Registro emocional")
        self._ensure_day_is_free(entry)
        self.items[entry.id] = entry

    def _ensure_day_is_free(self, entry: MoodEntry) -> None:
        if entry.is_deleted:
            return
        for other in self.items.values():
            if (
                other.id != entry.id
                and other.user_id == entry.user_id
                and other.date == entry.date
                and not other.is_deleted
            ):
                raise MoodEntryAlreadyExistsError(entry.date)

    def _active(
        self, user_id: UUID, date_from: dt.date | None, date_to: dt.date | None
    ) -> list[MoodEntry]:
        found = [
            e
            for e in self.items.values()
            if e.user_id == user_id
            and not e.is_deleted
            and (date_from is None or e.date >= date_from)
            and (date_to is None or e.date <= date_to)
        ]
        return sorted(found, key=lambda e: e.date, reverse=True)

    def _changes(self, user_id: UUID, updated_since: dt.datetime) -> list[MoodEntry]:
        found = [
            e for e in self.items.values() if e.user_id == user_id and e.updated_at > updated_since
        ]
        return sorted(found, key=lambda e: (e.updated_at, str(e.id)))


class FakeJournalEntryRepository:
    def __init__(self) -> None:
        self.items: dict[UUID, JournalEntry] = {}

    def add(self, entry: JournalEntry) -> None:
        if entry.id in self.items:
            raise DomainValidationError("id", "Ya existe una reflexión con ese id")
        self.items[entry.id] = entry

    def get(self, user_id: UUID, entry_id: UUID) -> JournalEntry | None:
        entry = self.get_any(user_id, entry_id)
        return entry if entry and not entry.is_deleted else None

    def get_any(self, user_id: UUID, entry_id: UUID) -> JournalEntry | None:
        entry = self.items.get(entry_id)
        return entry if entry and entry.user_id == user_id else None

    def list_entries(
        self, user_id: UUID, *, mood_entry_id: UUID | None, limit: int, offset: int
    ) -> list[JournalEntry]:
        return self._active(user_id, mood_entry_id)[offset : offset + limit]

    def count_entries(self, user_id: UUID, *, mood_entry_id: UUID | None) -> int:
        return len(self._active(user_id, mood_entry_id))

    def list_changes(
        self, user_id: UUID, *, updated_since: dt.datetime, limit: int, offset: int
    ) -> list[JournalEntry]:
        return self._changes(user_id, updated_since)[offset : offset + limit]

    def count_changes(self, user_id: UUID, *, updated_since: dt.datetime) -> int:
        return len(self._changes(user_id, updated_since))

    def update(self, entry: JournalEntry) -> None:
        if entry.id not in self.items:
            raise NotFoundError("Reflexión")
        self.items[entry.id] = entry

    def _active(self, user_id: UUID, mood_entry_id: UUID | None) -> list[JournalEntry]:
        found = [
            e
            for e in self.items.values()
            if e.user_id == user_id
            and not e.is_deleted
            and (mood_entry_id is None or e.mood_entry_id == mood_entry_id)
        ]
        return sorted(found, key=lambda e: e.created_at, reverse=True)

    def _changes(self, user_id: UUID, updated_since: dt.datetime) -> list[JournalEntry]:
        found = [
            e for e in self.items.values() if e.user_id == user_id and e.updated_at > updated_since
        ]
        return sorted(found, key=lambda e: (e.updated_at, str(e.id)))
