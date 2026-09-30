import datetime as dt
from uuid import UUID

from sqlalchemy import ColumnElement, delete, func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.domain.entities import MoodEntry
from app.domain.errors import DomainValidationError, MoodEntryAlreadyExistsError, NotFoundError
from app.domain.value_objects import MoodType
from app.infrastructure.db.models import MoodEntryModel
from app.infrastructure.repositories._db_errors import violated_constraint


def _to_model(entry: MoodEntry) -> MoodEntryModel:
    return MoodEntryModel(
        id=entry.id,
        user_id=entry.user_id,
        date=entry.date,
        mood=entry.mood.value,
        note=entry.note,
        created_at=entry.created_at,
        updated_at=entry.updated_at,
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
    )


def _conditions(
    user_id: UUID, date_from: dt.date | None, date_to: dt.date | None
) -> list[ColumnElement[bool]]:
    conditions: list[ColumnElement[bool]] = [MoodEntryModel.user_id == user_id]
    if date_from is not None:
        conditions.append(MoodEntryModel.date >= date_from)
    if date_to is not None:
        conditions.append(MoodEntryModel.date <= date_to)
    return conditions


class SqlAlchemyMoodEntryRepository:
    def __init__(self, session: Session) -> None:
        self._session = session

    def add(self, entry: MoodEntry) -> None:
        self._session.add(_to_model(entry))
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

    def get(self, user_id: UUID, entry_id: UUID) -> MoodEntry | None:
        model = self._get_model(user_id, entry_id)
        return _to_entity(model) if model else None

    def get_by_date(self, user_id: UUID, date: dt.date) -> MoodEntry | None:
        stmt = select(MoodEntryModel).where(
            MoodEntryModel.user_id == user_id, MoodEntryModel.date == date
        )
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
            .where(*_conditions(user_id, date_from, date_to))
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
            .where(*_conditions(user_id, date_from, date_to))
        )
        return self._session.scalar(stmt) or 0

    def update(self, entry: MoodEntry) -> None:
        model = self._get_model(entry.user_id, entry.id)
        if model is None:
            raise NotFoundError("Registro emocional")
        model.mood = entry.mood.value
        model.note = entry.note
        model.updated_at = entry.updated_at
        self._session.commit()

    def delete(self, user_id: UUID, entry_id: UUID) -> bool:
        result = self._session.execute(
            delete(MoodEntryModel).where(
                MoodEntryModel.user_id == user_id, MoodEntryModel.id == entry_id
            )
        )
        self._session.commit()
        return result.rowcount > 0

    def _get_model(self, user_id: UUID, entry_id: UUID) -> MoodEntryModel | None:
        stmt = select(MoodEntryModel).where(
            MoodEntryModel.user_id == user_id, MoodEntryModel.id == entry_id
        )
        return self._session.scalars(stmt).first()
