import datetime as dt
from collections.abc import Iterator
from uuid import uuid4

import pytest
from sqlalchemy import Engine, text
from sqlalchemy.orm import Session

import app.infrastructure.db.models  # noqa: F401
from app.domain.entities import User
from app.infrastructure.db.base import Base
from app.infrastructure.repositories.user_repository import SqlAlchemyUserRepository

NOW = dt.datetime(2026, 9, 29, 14, 30, tzinfo=dt.UTC)


@pytest.fixture(scope="session")
def schema(test_engine: Engine) -> None:
    Base.metadata.drop_all(test_engine)
    Base.metadata.create_all(test_engine)


@pytest.fixture
def session(test_engine: Engine, schema: None) -> Iterator[Session]:
    with Session(test_engine, expire_on_commit=False) as db_session:
        yield db_session
    with test_engine.begin() as connection:
        connection.execute(text("TRUNCATE users CASCADE"))


def make_user(session: Session, email: str) -> User:
    user = User(id=uuid4(), email=email, password_hash="hash", created_at=NOW, updated_at=NOW)
    SqlAlchemyUserRepository(session).add(user)
    return user


@pytest.fixture
def user(session: Session) -> User:
    return make_user(session, "ana@example.com")
