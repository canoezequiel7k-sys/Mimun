from uuid import UUID

from sqlalchemy import ColumnElement, delete, func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.domain.entities import JournalEntry
from app.domain.errors import DomainValidationError, NotFoundError
from app.infrastructure.db.models import JournalEntryModel
from app.infrastructure.repositories._db_errors import violated_constraint


def _to_model(entry: JournalEntry) -> JournalEntryModel:
    return JournalEntryModel(
        id=entry.id,
        user_id=entry.user_id,
        mood_entry_id=entry.mood_entry_id,
        title=entry.title,
        content=entry.content,
        created_at=entry.created_at,
        updated_at=entry.updated_at,
    )


def _to_entity(model: JournalEntryModel) -> JournalEntry:
    return JournalEntry(
        id=model.id,
        user_id=model.user_id,
        mood_entry_id=model.mood_entry_id,
        title=model.title,
        content=model.content,
        created_at=model.created_at,
        updated_at=model.updated_at,
    )


def _conditions(user_id: UUID, mood_entry_id: UUID | None) -> list[ColumnElement[bool]]:
    conditions: list[ColumnElement[bool]] = [JournalEntryModel.user_id == user_id]
    if mood_entry_id is not None:
        conditions.append(JournalEntryModel.mood_entry_id == mood_entry_id)
    return conditions


class SqlAlchemyJournalEntryRepository:
    def __init__(self, session: Session) -> None:
        self._session = session

    def add(self, entry: JournalEntry) -> None:
        self._session.add(_to_model(entry))
        self._commit_translating_errors()

    def get(self, user_id: UUID, entry_id: UUID) -> JournalEntry | None:
        model = self._get_model(user_id, entry_id)
        return _to_entity(model) if model else None

    def list_entries(
        self, user_id: UUID, *, mood_entry_id: UUID | None, limit: int, offset: int
    ) -> list[JournalEntry]:
        stmt = (
            select(JournalEntryModel)
            .where(*_conditions(user_id, mood_entry_id))
            .order_by(JournalEntryModel.created_at.desc(), JournalEntryModel.id)
            .limit(limit)
            .offset(offset)
        )
        return [_to_entity(model) for model in self._session.scalars(stmt)]

    def count_entries(self, user_id: UUID, *, mood_entry_id: UUID | None) -> int:
        stmt = (
            select(func.count())
            .select_from(JournalEntryModel)
            .where(*_conditions(user_id, mood_entry_id))
        )
        return self._session.scalar(stmt) or 0

    def update(self, entry: JournalEntry) -> None:
        model = self._get_model(entry.user_id, entry.id)
        if model is None:
            raise NotFoundError("Reflexión")
        model.mood_entry_id = entry.mood_entry_id
        model.title = entry.title
        model.content = entry.content
        model.updated_at = entry.updated_at
        self._commit_translating_errors()

    def delete(self, user_id: UUID, entry_id: UUID) -> bool:
        result = self._session.execute(
            delete(JournalEntryModel).where(
                JournalEntryModel.user_id == user_id, JournalEntryModel.id == entry_id
            )
        )
        self._session.commit()
        return result.rowcount > 0

    def _get_model(self, user_id: UUID, entry_id: UUID) -> JournalEntryModel | None:
        stmt = select(JournalEntryModel).where(
            JournalEntryModel.user_id == user_id, JournalEntryModel.id == entry_id
        )
        return self._session.scalars(stmt).first()

    def _commit_translating_errors(self) -> None:
        try:
            self._session.commit()
        except IntegrityError as exc:
            self._session.rollback()
            constraint = violated_constraint(exc)
            if constraint == "pk_journal_entries":
                raise DomainValidationError("id", "Ya existe una reflexión con ese id") from exc
            if constraint == "fk_journal_entries_mood_entry_id_mood_entries":
                raise DomainValidationError("mood_entry_id", "El registro no existe") from exc
            raise
