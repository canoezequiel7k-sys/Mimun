from dataclasses import dataclass
from uuid import UUID

from app.application.ports.clock import Clock
from app.domain.entities import JournalEntry
from app.domain.errors import DomainValidationError, NotFoundError
from app.domain.repositories import JournalEntryRepository, MoodEntryRepository

_RESOURCE = "Reflexión"


@dataclass(frozen=True, slots=True)
class CreateJournalEntryCommand:
    user_id: UUID
    mood_entry_id: UUID | None
    title: str | None
    content: str
    id: UUID | None = None


@dataclass(frozen=True, slots=True)
class UpdateJournalEntryCommand:
    user_id: UUID
    entry_id: UUID
    mood_entry_id: UUID | None
    title: str | None
    content: str


@dataclass(frozen=True, slots=True)
class ListJournalEntriesQuery:
    user_id: UUID
    mood_entry_id: UUID | None
    limit: int
    offset: int


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
        items = self._journal.list_entries(
            query.user_id,
            mood_entry_id=query.mood_entry_id,
            limit=query.limit,
            offset=query.offset,
        )
        total = self._journal.count_entries(query.user_id, mood_entry_id=query.mood_entry_id)
        return JournalEntryPage(items=items, total=total, limit=query.limit, offset=query.offset)


class UpdateJournalEntry:
    def __init__(
        self, journal: JournalEntryRepository, moods: MoodEntryRepository, clock: Clock
    ) -> None:
        self._journal = journal
        self._moods = moods
        self._clock = clock

    def execute(self, command: UpdateJournalEntryCommand) -> JournalEntry:
        entry = self._journal.get(command.user_id, command.entry_id)
        if entry is None:
            raise NotFoundError(_RESOURCE)
        _ensure_mood_entry_exists(self._moods, command.user_id, command.mood_entry_id)
        entry.update(
            mood_entry_id=command.mood_entry_id,
            title=command.title,
            content=command.content,
            now=self._clock.now(),
        )
        self._journal.update(entry)
        return entry


class DeleteJournalEntry:
    def __init__(self, journal: JournalEntryRepository) -> None:
        self._journal = journal

    def execute(self, user_id: UUID, entry_id: UUID) -> None:
        if not self._journal.delete(user_id, entry_id):
            raise NotFoundError(_RESOURCE)


@dataclass(frozen=True, slots=True)
class JournalEntryUseCases:
    create: CreateJournalEntry
    get: GetJournalEntry
    list_entries: ListJournalEntries
    update: UpdateJournalEntry
    delete: DeleteJournalEntry

    @classmethod
    def build(
        cls, journal: JournalEntryRepository, moods: MoodEntryRepository, clock: Clock
    ) -> "JournalEntryUseCases":
        return cls(
            create=CreateJournalEntry(journal, moods, clock),
            get=GetJournalEntry(journal),
            list_entries=ListJournalEntries(journal),
            update=UpdateJournalEntry(journal, moods, clock),
            delete=DeleteJournalEntry(journal),
        )
