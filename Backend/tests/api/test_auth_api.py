import datetime as dt
from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient

from app.application.use_cases.auth import AuthUseCases
from app.main import create_app
from app.presentation.api.deps import get_auth_use_cases
from tests.fakes import FakeClock
from tests.fakes_auth import (
    FakePasswordHasher,
    FakeRefreshTokenRepository,
    FakeTokenService,
    FakeUserRepository,
)

BASE = "/api/v1/auth"
CREDENTIALS = {"email": "Ana@Example.com", "password": "una-clave-larga"}


@pytest.fixture
def client() -> Iterator[TestClient]:
    app = create_app()
    use_cases = AuthUseCases.build(
        users=FakeUserRepository(),
        refresh_tokens=FakeRefreshTokenRepository(),
        hasher=FakePasswordHasher(),
        tokens=FakeTokenService(),
        clock=FakeClock(dt.datetime(2026, 9, 30, 12, 0, tzinfo=dt.UTC)),
    )
    app.dependency_overrides[get_auth_use_cases] = lambda: use_cases
    with TestClient(app) as test_client:
        yield test_client


def test_register_returns_201_with_contract_shape(client: TestClient) -> None:
    response = client.post(f"{BASE}/register", json=CREDENTIALS)
    assert response.status_code == 201
    data = response.json()
    assert set(data) == {"user", "access_token", "refresh_token", "token_type", "expires_in"}
    assert data["user"]["email"] == "ana@example.com"
    assert data["token_type"] == "bearer" and data["expires_in"] == 1800
    assert "una-clave-larga" not in response.text


def test_register_duplicate_email_returns_409(client: TestClient) -> None:
    client.post(f"{BASE}/register", json=CREDENTIALS)
    response = client.post(f"{BASE}/register", json=CREDENTIALS)
    assert response.status_code == 409
    assert response.json()["error"]["code"] == "EMAIL_ALREADY_REGISTERED"


def test_register_short_password_returns_422_on_the_password_field(client: TestClient) -> None:
    response = client.post(f"{BASE}/register", json={**CREDENTIALS, "password": "corta"})
    assert response.status_code == 422
    assert response.json()["error"]["details"][0]["field"] == "password"


def test_login_ok_and_wrong_password(client: TestClient) -> None:
    client.post(f"{BASE}/register", json=CREDENTIALS)
    assert client.post(f"{BASE}/login", json=CREDENTIALS).status_code == 200

    response = client.post(f"{BASE}/login", json={**CREDENTIALS, "password": "otra-clave-larga"})
    assert response.status_code == 401
    assert response.json()["error"]["code"] == "UNAUTHORIZED"
    assert response.headers["WWW-Authenticate"] == "Bearer"


def test_refresh_rotates_and_the_old_token_stops_working(client: TestClient) -> None:
    old = client.post(f"{BASE}/register", json=CREDENTIALS).json()["refresh_token"]
    response = client.post(f"{BASE}/refresh", json={"refresh_token": old})
    assert response.status_code == 200
    assert "user" not in response.json()
    assert response.json()["refresh_token"] != old
    assert client.post(f"{BASE}/refresh", json={"refresh_token": old}).status_code == 401


def test_logout_returns_204_and_revokes_the_session(client: TestClient) -> None:
    token = client.post(f"{BASE}/register", json=CREDENTIALS).json()["refresh_token"]
    assert client.post(f"{BASE}/logout", json={"refresh_token": token}).status_code == 204
    assert client.post(f"{BASE}/logout", json={"refresh_token": "desconocido"}).status_code == 204
    assert client.post(f"{BASE}/refresh", json={"refresh_token": token}).status_code == 401
