import datetime as dt
from dataclasses import dataclass
from uuid import UUID

from app.application.ports.clock import Clock
from app.domain.entities import MoodEntry
from app.domain.errors import DomainValidationError, NotFoundError
from app.domain.repositories import MoodEntryRepository
from app.domain.value_objects import MoodType

_RESOURCE = "Registro emocional"


@dataclass(frozen=True, slots=True)
class CreateMoodEntryCommand:
    user_id: UUID
    date: dt.date
    mood: MoodType
    note: str | None
    id: UUID | None = None


@dataclass(frozen=True, slots=True)
class UpdateMoodEntryCommand:
    user_id: UUID
    entry_id: UUID
    mood: MoodType
    note: str | None


@dataclass(frozen=True, slots=True)
class ListMoodEntriesQuery:
    user_id: UUID
    date_from: dt.date | None
    date_to: dt.date | None
    limit: int
    offset: int


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
        latest_allowed = now.date() + dt.timedelta(days=1)  # tolera husos horarios
        if command.date > latest_allowed:
            raise DomainValidationError("date", "No puede ser posterior a mañana")
        entry = MoodEntry.create(
            user_id=command.user_id,
            date=command.date,
            mood=command.mood,
            note=command.note,
            now=now,
            id=command.id,
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


class UpdateMoodEntry:
    def __init__(self, repository: MoodEntryRepository, clock: Clock) -> None:
        self._repository = repository
        self._clock = clock

    def execute(self, command: UpdateMoodEntryCommand) -> MoodEntry:
        entry = self._repository.get(command.user_id, command.entry_id)
        if entry is None:
            raise NotFoundError(_RESOURCE)
        entry.update(mood=command.mood, note=command.note, now=self._clock.now())
        self._repository.update(entry)
        return entry


class DeleteMoodEntry:
    def __init__(self, repository: MoodEntryRepository) -> None:
        self._repository = repository

    def execute(self, user_id: UUID, entry_id: UUID) -> None:
        if not self._repository.delete(user_id, entry_id):
            raise NotFoundError(_RESOURCE)


@dataclass(frozen=True, slots=True)
class MoodEntryUseCases:
    create: CreateMoodEntry
    get: GetMoodEntry
    list_entries: ListMoodEntries
    update: UpdateMoodEntry
    delete: DeleteMoodEntry

    @classmethod
    def build(cls, repository: MoodEntryRepository, clock: Clock) -> "MoodEntryUseCases":
        return cls(
            create=CreateMoodEntry(repository, clock),
            get=GetMoodEntry(repository),
            list_entries=ListMoodEntries(repository),
            update=UpdateMoodEntry(repository, clock),
            delete=DeleteMoodEntry(repository),
        )
