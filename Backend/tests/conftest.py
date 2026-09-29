from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import Engine, create_engine, text
from sqlalchemy.engine import make_url

from app.infrastructure.config import get_settings
from app.main import create_app


@pytest.fixture
def client() -> Iterator[TestClient]:
    """Cliente HTTP de la app. No requiere base de datos."""
    with TestClient(create_app()) as test_client:
        yield test_client


@pytest.fixture(scope="session")
def test_engine() -> Iterator[Engine]:
    """Engine de la base de datos de test. Crea `mimun_test` si no existe.

    Nunca usa la base de desarrollo. Solo lo piden los tests marcados como `integration`.
    """
    settings = get_settings()
    test_url = make_url(settings.test_database_url)
    dev_url = make_url(settings.database_url)

    if test_url.database == dev_url.database:
        raise RuntimeError("TEST_DATABASE_URL apunta a la misma base que DATABASE_URL")

    admin_engine = create_engine(test_url.set(database="postgres"), isolation_level="AUTOCOMMIT")
    with admin_engine.connect() as connection:
        exists = connection.execute(
            text("SELECT 1 FROM pg_database WHERE datname = :name"),
            {"name": test_url.database},
        ).scalar()
        if not exists:
            connection.execute(text(f'CREATE DATABASE "{test_url.database}"'))
    admin_engine.dispose()

    engine = create_engine(test_url)
    yield engine
    engine.dispose()
