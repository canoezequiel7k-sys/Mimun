"""Composition root.

Único lugar donde se conectan las implementaciones concretas (infraestructura)
con el resto de la app. Los casos de uso se irán armando acá a partir de B3.
"""

from dataclasses import dataclass, field

from sqlalchemy import Engine
from sqlalchemy.orm import Session, sessionmaker

from app.infrastructure.config import Settings
from app.infrastructure.db.session import build_engine, build_session_factory


@dataclass
class Container:
    settings: Settings
    _engine: Engine | None = field(default=None, init=False, repr=False)
    _session_factory: sessionmaker[Session] | None = field(default=None, init=False, repr=False)

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

    def dispose(self) -> None:
        if self._engine is not None:
            self._engine.dispose()
            self._engine = None
            self._session_factory = None
