import datetime as dt
from uuid import UUID

from sqlalchemy import select, update
from sqlalchemy.orm import Session

from app.domain.entities import RefreshToken
from app.infrastructure.db.models import RefreshTokenModel


def _to_entity(model: RefreshTokenModel) -> RefreshToken:
    return RefreshToken(
        id=model.id,
        user_id=model.user_id,
        token_hash=model.token_hash,
        expires_at=model.expires_at,
        created_at=model.created_at,
        revoked_at=model.revoked_at,
    )


class SqlAlchemyRefreshTokenRepository:
    def __init__(self, session: Session) -> None:
        self._session = session

    def add(self, token: RefreshToken) -> None:
        self._session.add(
            RefreshTokenModel(
                id=token.id,
                user_id=token.user_id,
                token_hash=token.token_hash,
                expires_at=token.expires_at,
                revoked_at=token.revoked_at,
                created_at=token.created_at,
            )
        )
        self._session.commit()

    def get_by_hash(self, token_hash: str) -> RefreshToken | None:
        stmt = select(RefreshTokenModel).where(RefreshTokenModel.token_hash == token_hash)
        model = self._session.scalars(stmt).first()
        return _to_entity(model) if model else None

    def revoke(self, token_id: UUID, now: dt.datetime) -> bool:
        result = self._session.execute(
            update(RefreshTokenModel)
            .where(RefreshTokenModel.id == token_id, RefreshTokenModel.revoked_at.is_(None))
            .values(revoked_at=now)
        )
        self._session.commit()
        return result.rowcount > 0
