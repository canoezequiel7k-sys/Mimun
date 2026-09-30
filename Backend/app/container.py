"""Composition root.

Único lugar donde se conectan las implementaciones concretas (infraestructura)
con los casos de uso.
"""

from collections.abc import Iterator
from contextlib import contextmanager
from dataclasses import dataclass, field
from uuid import UUID

from sqlalchemy import Engine
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session, sessionmaker

from app.application.use_cases.mood_entries import MoodEntryUseCases
from app.domain.entities import User
from app.infrastructure.clock import SystemClock
from app.infrastructure.config import Settings
from app.infrastructure.db.session import build_engine, build_session_factory
from app.infrastructure.repositories.mood_entry_repository import SqlAlchemyMoodEntryRepository
from app.infrastructure.repositories.user_repository import SqlAlchemyUserRepository

# Usuario fijo de desarrollo, solo hasta la Fase B6 (autenticación real).
DEV_USER_ID = UUID("00000000-0000-4000-8000-000000000001")


@dataclass
class Container:
    settings: Settings
    _engine: Engine | None = field(default=None, init=False, repr=False)
    _session_factory: sessionmaker[Session] | None = field(default=None, init=False, repr=False)
    _clock: SystemClock = field(default_factory=SystemClock, init=False, repr=False)
    _dev_user_ready: bool = field(default=False, init=False, repr=False)

    @property
    def engine(self) -> Engine:
        if self._engine is None:
            self._engine = build_engine(self.settings.database_url)
        return self._engine

    @property
    def session_factory(self) -> sessionmaker[Session]:
        if self._session_factory is None:
            self._session_factory = build_session_factory(self.engine)
        return self._session_factory

    @contextmanager
    def mood_entry_use_cases(self) -> Iterator[MoodEntryUseCases]:
        """Una sesión de base de datos por request."""
        with self.session_factory() as session:
            repository = SqlAlchemyMoodEntryRepository(session)
            yield MoodEntryUseCases.build(repository, self._clock)

    def dev_user_id(self) -> UUID:
        """Usuario de desarrollo (se crea la primera vez). Nunca en producción."""
        if self.settings.environment == "production":
            raise RuntimeError("El usuario de desarrollo no puede usarse en producción")
        if not self._dev_user_ready:
            with self.session_factory() as session:
                users = SqlAlchemyUserRepository(session)
                if users.get_by_id(DEV_USER_ID) is None:
                    now = self._clock.now()
                    user = User(
                        id=DEV_USER_ID,
                        email="dev@mimun.local",
                        password_hash="!",
                        created_at=now,
                        updated_at=now,
                    )
                    try:
                        users.add(user)
                    except IntegrityError:  # otro request lo creó a la vez
                        session.rollback()
            self._dev_user_ready = True
        return DEV_USER_ID

    def dispose(self) -> None:
        if self._engine is not None:
            self._engine.dispose()
            self._engine = None
            self._session_factory = None
