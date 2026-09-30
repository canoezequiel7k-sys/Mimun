import datetime as dt
from uuid import uuid4

import pytest
from sqlalchemy.orm import Session

from app.domain.entities import RefreshToken, User
from app.domain.errors import EmailAlreadyRegisteredError
from app.infrastructure.repositories.refresh_token_repository import (
    SqlAlchemyRefreshTokenRepository,
)
from tests.integration.conftest import NOW, make_user

pytestmark = pytest.mark.integration


def _token(user: User, token_hash: str = "hash-1") -> RefreshToken:
    return RefreshToken(
        id=uuid4(),
        user_id=user.id,
        token_hash=token_hash,
        expires_at=NOW + dt.timedelta(days=14),
        created_at=NOW,
    )


def test_refresh_token_roundtrip_and_single_revocation(session: Session, user: User) -> None:
    repo = SqlAlchemyRefreshTokenRepository(session)
    token = _token(user)
    repo.add(token)
    assert repo.get_by_hash("hash-1") == token

    assert repo.revoke(token.id, NOW) is True
    assert repo.revoke(token.id, NOW) is False
    stored = repo.get_by_hash("hash-1")
    assert stored is not None and stored.revoked_at is not None


def test_unknown_hash_returns_none(session: Session, user: User) -> None:
    assert SqlAlchemyRefreshTokenRepository(session).get_by_hash("nope") is None


def test_duplicate_email_is_translated_ignoring_case(session: Session, user: User) -> None:
    with pytest.raises(EmailAlreadyRegisteredError):
        make_user(session, "ANA@example.com")
