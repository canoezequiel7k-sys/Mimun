import datetime as dt
from dataclasses import dataclass
from uuid import UUID

from app.application.ports.clock import Clock
from app.domain.entities import JournalEntry
from app.domain.errors import DomainValidationError, NotFoundError
from app.domain.repositories import JournalEntryRepository, MoodEntryRepository
from app.domain.sync import require_aware, resolve_client_instant

_RESOURCE = "Reflexión"


@dataclass(frozen=True, slots=True)
class CreateJournalEntryCommand:
    user_id: UUID
    mood_entry_id: UUID | None
    title: str | None
    content: str
    id: UUID | None = None
    created_at: dt.datetime | None = None
    edited_at: dt.datetime | None = None


@dataclass(frozen=True, slots=True)
class UpsertJournalEntryCommand:
    user_id: UUID
    entry_id: UUID
    mood_entry_id: UUID | None
    title: str | None
    content: str
    created_at: dt.datetime | None = None  # solo se usa al crear
    edited_at: dt.datetime | None = None


@dataclass(frozen=True, slots=True)
class JournalUpsertResult:
    entry: JournalEntry
    created: bool


@dataclass(frozen=True, slots=True)
class ListJournalEntriesQuery:
    user_id: UUID
    mood_entry_id: UUID | None
    limit: int
    offset: int
    updated_since: dt.datetime | None = None


@dataclass(frozen=True, slots=True)
class JournalEntryPage:
    items: list[JournalEntry]
    total: int
    limit: int
    offset: int


def _ensure_mood_entry_exists(
    repository: MoodEntryRepository, user_id: UUID, mood_entry_id: UUID | None
) -> None:
    if mood_entry_id is not None and repository.get(user_id, mood_entry_id) is None:
        raise DomainValidationError("mood_entry_id", "El registro emocional no existe")


class CreateJournalEntry:
    def __init__(
        self, journal: JournalEntryRepository, moods: MoodEntryRepository, clock: Clock
    ) -> None:
        self._journal = journal
        self._moods = moods
        self._clock = clock

    def execute(self, command: CreateJournalEntryCommand) -> JournalEntry:
        _ensure_mood_entry_exists(self._moods, command.user_id, command.mood_entry_id)
        entry = JournalEntry.create(
            user_id=command.user_id,
            mood_entry_id=command.mood_entry_id,
            title=command.title,
            content=command.content,
            now=self._clock.now(),
            id=command.id,
            created_at=command.created_at,
            edited_at=command.edited_at,
        )
        self._journal.add(entry)
        return entry


class GetJournalEntry:
    def __init__(self, journal: JournalEntryRepository) -> None:
        self._journal = journal

    def execute(self, user_id: UUID, entry_id: UUID) -> JournalEntry:
        entry = self._journal.get(user_id, entry_id)
        if entry is None:
            raise NotFoundError(_RESOURCE)
        return entry


class ListJournalEntries:
    def __init__(self, journal: JournalEntryRepository) -> None:
        self._journal = journal

    def execute(self, query: ListJournalEntriesQuery) -> JournalEntryPage:
        if query.updated_since is not None:
            return self._changes(query)
        items = self._journal.list_entries(
            query.user_id,
            mood_entry_id=query.mood_entry_id,
            limit=query.limit,
            offset=query.offset,
        )
        total = self._journal.count_entries(query.user_id, mood_entry_id=query.mood_entry_id)
        return JournalEntryPage(items=items, total=total, limit=query.limit, offset=query.offset)

    def _changes(self, query: ListJournalEntriesQuery) -> JournalEntryPage:
        if query.mood_entry_id is not None:
            raise DomainValidationError("updated_since", "No se puede combinar con mood_entry_id")
        since = require_aware(query.updated_since, "updated_since")  # type: ignore[arg-type]
        items = self._journal.list_changes(
            query.user_id, updated_since=since, limit=query.limit, offset=query.offset
        )
        total = self._journal.count_changes(query.user_id, updated_since=since)
        return JournalEntryPage(items=items, total=total, limit=query.limit, offset=query.offset)


class UpsertJournalEntry:
    """Crea o actualiza por id. Idempotente; resuelve conflictos por `edited_at`."""

    def __init__(
        self, journal: JournalEntryRepository, moods: MoodEntryRepository, clock: Clock
    ) -> None:
        self._journal = journal
        self._moods = moods
        self._clock = clock

    def execute(self, command: UpsertJournalEntryCommand) -> JournalUpsertResult:
        now = self._clock.now()
        existing = self._journal.get_any(command.user_id, command.entry_id)

        if existing is None:
            _ensure_mood_entry_exists(self._moods, command.user_id, command.mood_entry_id)
            entry = JournalEntry.create(
                user_id=command.user_id,
                mood_entry_id=command.mood_entry_id,
                title=command.title,
                content=command.content,
                now=now,
                id=command.entry_id,
                created_at=command.created_at,
                edited_at=command.edited_at,
            )
            try:
                self._journal.add(entry)
            except DomainValidationError as exc:
                if exc.field == "id":
                    raise NotFoundError(_RESOURCE) from exc
                raise
            return JournalUpsertResult(entry=entry, created=True)

        incoming = resolve_client_instant(command.edited_at, now, field="edited_at")
        if incoming < existing.edited_at:  # el servidor tiene algo más nuevo: gana el servidor
            return JournalUpsertResult(entry=existing, created=False)

        _ensure_mood_entry_exists(self._moods, command.user_id, command.mood_entry_id)
        existing.update(
            mood_entry_id=command.mood_entry_id,
            title=command.title,
            content=command.content,
            now=now,
            edited_at=incoming,
        )
        self._journal.update(existing)
        return JournalUpsertResult(entry=existing, created=False)


class DeleteJournalEntry:
    """Borrado lógico e idempotente."""

    def __init__(self, journal: JournalEntryRepository, clock: Clock) -> None:
        self._journal = journal
        self._clock = clock

    def execute(self, user_id: UUID, entry_id: UUID) -> None:
        entry = self._journal.get_any(user_id, entry_id)
        if entry is None:
            raise NotFoundError(_RESOURCE)
        if entry.is_deleted:
            return
        entry.mark_deleted(self._clock.now())
        self._journal.update(entry)


@dataclass(frozen=True, slots=True)
class JournalEntryUseCases:
    create: CreateJournalEntry
    get: GetJournalEntry
    list_entries: ListJournalEntries
    upsert: UpsertJournalEntry
    delete: DeleteJournalEntry

    @classmethod
    def build(
        cls, journal: JournalEntryRepository, moods: MoodEntryRepository, clock: Clock
    ) -> "JournalEntryUseCases":
        return cls(
            create=CreateJournalEntry(journal, moods, clock),
            get=GetJournalEntry(journal),
            list_entries=ListJournalEntries(journal),
            upsert=UpsertJournalEntry(journal, moods, clock),
            delete=DeleteJournalEntry(journal, clock),
        )
