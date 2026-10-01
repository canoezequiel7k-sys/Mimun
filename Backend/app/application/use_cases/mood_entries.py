import datetime as dt
from dataclasses import dataclass
from uuid import UUID

from app.application.ports.clock import Clock
from app.domain.entities import MoodEntry
from app.domain.errors import DomainValidationError, NotFoundError
from app.domain.repositories import MoodEntryRepository
from app.domain.sync import require_aware, resolve_client_instant
from app.domain.value_objects import MoodType

_RESOURCE = "Registro emocional"


def _ensure_date_allowed(date: dt.date, now: dt.datetime) -> None:
    if date > now.date() + dt.timedelta(days=1):  # tolera husos horarios
        raise DomainValidationError("date", "No puede ser posterior a mañana")


@dataclass(frozen=True, slots=True)
class CreateMoodEntryCommand:
    user_id: UUID
    date: dt.date
    mood: MoodType
    note: str | None
    id: UUID | None = None
    edited_at: dt.datetime | None = None


@dataclass(frozen=True, slots=True)
class UpsertMoodEntryCommand:
    user_id: UUID
    entry_id: UUID
    mood: MoodType
    note: str | None
    date: dt.date | None = None  # obligatoria solo al crear
    edited_at: dt.datetime | None = None


@dataclass(frozen=True, slots=True)
class UpsertResult:
    entry: MoodEntry
    created: bool


@dataclass(frozen=True, slots=True)
class ListMoodEntriesQuery:
    user_id: UUID
    date_from: dt.date | None
    date_to: dt.date | None
    limit: int
    offset: int
    updated_since: dt.datetime | None = None


@dataclass(frozen=True, slots=True)
class MoodEntryPage:
    items: list[MoodEntry]
    total: int
    limit: int
    offset: int


class CreateMoodEntry:
    def __init__(self, repository: MoodEntryRepository, clock: Clock) -> None:
        self._repository = repository
        self._clock = clock

    def execute(self, command: CreateMoodEntryCommand) -> MoodEntry:
        now = self._clock.now()
        _ensure_date_allowed(command.date, now)
        entry = MoodEntry.create(
            user_id=command.user_id,
            date=command.date,
            mood=command.mood,
            note=command.note,
            now=now,
            id=command.id,
            edited_at=command.edited_at,
        )
        self._repository.add(entry)
        return entry


class GetMoodEntry:
    def __init__(self, repository: MoodEntryRepository) -> None:
        self._repository = repository

    def execute(self, user_id: UUID, entry_id: UUID) -> MoodEntry:
        entry = self._repository.get(user_id, entry_id)
        if entry is None:
            raise NotFoundError(_RESOURCE)
        return entry


class ListMoodEntries:
    def __init__(self, repository: MoodEntryRepository) -> None:
        self._repository = repository

    def execute(self, query: ListMoodEntriesQuery) -> MoodEntryPage:
        if query.updated_since is not None:
            return self._changes(query)
        if query.date_from and query.date_to and query.date_from > query.date_to:
            raise DomainValidationError("to", "Debe ser mayor o igual a 'from'")
        items = self._repository.list_entries(
            query.user_id,
            date_from=query.date_from,
            date_to=query.date_to,
            limit=query.limit,
            offset=query.offset,
        )
        total = self._repository.count_entries(
            query.user_id, date_from=query.date_from, date_to=query.date_to
        )
        return MoodEntryPage(items=items, total=total, limit=query.limit, offset=query.offset)

    def _changes(self, query: ListMoodEntriesQuery) -> MoodEntryPage:
        if query.date_from or query.date_to:
            raise DomainValidationError("updated_since", "No se puede combinar con from/to")
        since = require_aware(query.updated_since, "updated_since")  # type: ignore[arg-type]
        items = self._repository.list_changes(
            query.user_id, updated_since=since, limit=query.limit, offset=query.offset
        )
        total = self._repository.count_changes(query.user_id, updated_since=since)
        return MoodEntryPage(items=items, total=total, limit=query.limit, offset=query.offset)


class UpsertMoodEntry:
    """Crea o actualiza por id. Idempotente; resuelve conflictos por `edited_at`."""

    def __init__(self, repository: MoodEntryRepository, clock: Clock) -> None:
        self._repository = repository
        self._clock = clock

    def execute(self, command: UpsertMoodEntryCommand) -> UpsertResult:
        now = self._clock.now()
        existing = self._repository.get_any(command.user_id, command.entry_id)

        if existing is None:
            if command.date is None:
                raise NotFoundError(_RESOURCE)
            _ensure_date_allowed(command.date, now)
            entry = MoodEntry.create(
                user_id=command.user_id,
                date=command.date,
                mood=command.mood,
                note=command.note,
                now=now,
                id=command.entry_id,
                edited_at=command.edited_at,
            )
            self._repository.add(entry)
            return UpsertResult(entry=entry, created=True)

        if command.date is not None and command.date != existing.date:
            raise DomainValidationError("date", "No se puede cambiar la fecha de un registro")

        incoming = resolve_client_instant(command.edited_at, now, field="edited_at")
        if incoming < existing.edited_at:  # el servidor tiene algo más nuevo: gana el servidor
            return UpsertResult(entry=existing, created=False)

        existing.update(mood=command.mood, note=command.note, now=now, edited_at=incoming)
        self._repository.update(existing)
        return UpsertResult(entry=existing, created=False)


class DeleteMoodEntry:
    """Borrado lógico e idempotente."""

    def __init__(self, repository: MoodEntryRepository, clock: Clock) -> None:
        self._repository = repository
        self._clock = clock

    def execute(self, user_id: UUID, entry_id: UUID) -> None:
        entry = self._repository.get_any(user_id, entry_id)
        if entry is None:
            raise NotFoundError(_RESOURCE)
        if entry.is_deleted:
            return
        entry.mark_deleted(self._clock.now())
        self._repository.update(entry)


@dataclass(frozen=True, slots=True)
class MoodEntryUseCases:
    create: CreateMoodEntry
    get: GetMoodEntry
    list_entries: ListMoodEntries
    upsert: UpsertMoodEntry
    delete: DeleteMoodEntry

    @classmethod
    def build(cls, repository: MoodEntryRepository, clock: Clock) -> "MoodEntryUseCases":
        return cls(
            create=CreateMoodEntry(repository, clock),
            get=GetMoodEntry(repository),
            list_entries=ListMoodEntries(repository),
            upsert=UpsertMoodEntry(repository, clock),
            delete=DeleteMoodEntry(repository, clock),
        )
