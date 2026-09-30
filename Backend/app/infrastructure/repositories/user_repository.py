from uuid import UUID

from sqlalchemy import func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.domain.entities import User
from app.domain.errors import EmailAlreadyRegisteredError
from app.infrastructure.db.models import UserModel
from app.infrastructure.repositories._db_errors import violated_constraint


def _to_entity(model: UserModel) -> User:
    return User(
        id=model.id,
        email=model.email,
        password_hash=model.password_hash,
        created_at=model.created_at,
        updated_at=model.updated_at,
    )


class SqlAlchemyUserRepository:
    def __init__(self, session: Session) -> None:
        self._session = session

    def add(self, user: User) -> None:
        self._session.add(
            UserModel(
                id=user.id,
                email=user.email,
                password_hash=user.password_hash,
                created_at=user.created_at,
                updated_at=user.updated_at,
            )
        )
        try:
            self._session.commit()
        except IntegrityError as exc:
            self._session.rollback()
            if violated_constraint(exc) == "uq_users_email_lower":
                raise EmailAlreadyRegisteredError() from exc
            raise

    def get_by_id(self, user_id: UUID) -> User | None:
        model = self._session.get(UserModel, user_id)
        return _to_entity(model) if model else None

    def get_by_email(self, email: str) -> User | None:
        stmt = select(UserModel).where(func.lower(UserModel.email) == email.lower())
        model = self._session.scalars(stmt).first()
        return _to_entity(model) if model else None
