import datetime as dt
from uuid import UUID

from sqlalchemy import ColumnElement, func, select
from sqlalchemy import update as sql_update
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.domain.entities import MoodEntry
from app.domain.errors import DomainValidationError, MoodEntryAlreadyExistsError, NotFoundError
from app.domain.value_objects import MoodType
from app.infrastructure.db.models import JournalEntryModel, MoodEntryModel
from app.infrastructure.repositories._db_errors import violated_constraint

_RESOURCE = "Registro emocional"


def _to_model(entry: MoodEntry) -> MoodEntryModel:
    return MoodEntryModel(
        id=entry.id,
        user_id=entry.user_id,
        date=entry.date,
        mood=entry.mood.value,
        note=entry.note,
        created_at=entry.created_at,
        updated_at=entry.updated_at,
        edited_at=entry.edited_at,
        deleted_at=entry.deleted_at,
    )


def _to_entity(model: MoodEntryModel) -> MoodEntry:
    return MoodEntry(
        id=model.id,
        user_id=model.user_id,
        date=model.date,
        mood=MoodType(model.mood),
        note=model.note,
        created_at=model.created_at,
        updated_at=model.updated_at,
        edited_at=model.edited_at,
        deleted_at=model.deleted_at,
    )


def _active_conditions(
    user_id: UUID, date_from: dt.date | None, date_to: dt.date | None
) -> list[ColumnElement[bool]]:
    conditions: list[ColumnElement[bool]] = [
        MoodEntryModel.user_id == user_id,
        MoodEntryModel.deleted_at.is_(None),
    ]
    if date_from is not None:
        conditions.append(MoodEntryModel.date >= date_from)
    if date_to is not None:
        conditions.append(MoodEntryModel.date <= date_to)
    return conditions


def _change_conditions(user_id: UUID, updated_since: dt.datetime) -> list[ColumnElement[bool]]:
    return [MoodEntryModel.user_id == user_id, MoodEntryModel.updated_at > updated_since]


class SqlAlchemyMoodEntryRepository:
    def __init__(self, session: Session) -> None:
        self._session = session

    def add(self, entry: MoodEntry) -> None:
        self._session.add(_to_model(entry))
        self._commit_translating_errors(entry)

    def get(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None:
        model = self._find(user_id, entry_id, include_deleted=False)
        return _to_entity(model) if model else None

    def get_any(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None:
        model = self._find(user_id, entry_id, include_deleted=True)
        return _to_entity(model) if model else None

    def get_by_date(self, user_id: UUID, date: dt.date) -> MoodEntry | None:
        stmt = select(MoodEntryModel).where(*_active_conditions(user_id, date, date))
        model = self._session.scalars(stmt).first()
        return _to_entity(model) if model else None

    def list_entries(
        self,
        user_id: UUID,
        *,
        date_from: dt.date | None,
        date_to: dt.date | None,
        limit: int,
        offset: int,
    ) -> list[MoodEntry]:
        stmt = (
            select(MoodEntryModel)
            .where(*_active_conditions(user_id, date_from, date_to))
            .order_by(MoodEntryModel.date.desc())
            .limit(limit)
            .offset(offset)
        )
        return [_to_entity(model) for model in self._session.scalars(stmt)]

    def count_entries(
        self, user_id: UUID, *, date_from: dt.date | None, date_to: dt.date | None
    ) -> int:
        stmt = (
            select(func.count())
            .select_from(MoodEntryModel)
            .where(*_active_conditions(user_id, date_from, date_to))
        )
        return self._session.scalar(stmt) or 0

    def list_changes(
        self, user_id: UUID, *, updated_since: dt.datetime, limit: int, offset: int
    ) -> list[MoodEntry]:
        stmt = (
            select(MoodEntryModel)
            .where(*_change_conditions(user_id, updated_since))
            .order_by(MoodEntryModel.updated_at, MoodEntryModel.id)
            .limit(limit)
            .offset(offset)
        )
        return [_to_entity(model) for model in self._session.scalars(stmt)]

    def count_changes(self, user_id: UUID, *, updated_since: dt.datetime) -> int:
        stmt = (
            select(func.count())
            .select_from(MoodEntryModel)
            .where(*_change_conditions(user_id, updated_since))
        )
        return self._session.scalar(stmt) or 0

    def update(self, entry: MoodEntry) -> None:
        model = self._find(entry.user_id, entry.id, include_deleted=True)
        if model is None:
            raise NotFoundError(_RESOURCE)
        model.mood = entry.mood.value
        model.note = entry.note
        model.updated_at = entry.updated_at
        model.edited_at = entry.edited_at
        model.deleted_at = entry.deleted_at
        if entry.deleted_at is not None:
            # Equivale al antiguo ON DELETE SET NULL: las reflexiones quedan sin asociación
            # y su updated_at cambia para que el cliente las descargue en el próximo pull.
            self._session.execute(
                sql_update(JournalEntryModel)
                .where(
                    JournalEntryModel.user_id == entry.user_id,
                    JournalEntryModel.mood_entry_id == entry.id,
                )
                .values(mood_entry_id=None, updated_at=entry.updated_at)
            )
        self._commit_translating_errors(entry)

    def _find(
        self, user_id: UUID, entry_id: UUID, *, include_deleted: bool
    ) -> MoodEntryModel | None:
        conditions = [MoodEntryModel.user_id == user_id, MoodEntryModel.id == entry_id]
        if not include_deleted:
            conditions.append(MoodEntryModel.deleted_at.is_(None))
        return self._session.scalars(select(MoodEntryModel).where(*conditions)).first()

    def _commit_translating_errors(self, entry: MoodEntry) -> None:
        try:
            self._session.commit()
        except IntegrityError as exc:
            self._session.rollback()
            constraint = violated_constraint(exc)
            if constraint == "uq_mood_entries_user_id_date":
                raise MoodEntryAlreadyExistsError(entry.date) from exc
            if constraint == "pk_mood_entries":
                raise DomainValidationError("id", "Ya existe un registro con ese id") from exc
            raise
