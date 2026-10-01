import datetime as dt
from uuid import uuid4

import pytest

from app.application.use_cases.auth import AuthenticateAccessToken, AuthUseCases
from app.domain.errors import (
    AuthenticationError,
    DomainValidationError,
    EmailAlreadyRegisteredError,
)
from tests.fakes import FakeClock
from tests.fakes_auth import (
    FakePasswordHasher,
    FakeRefreshTokenRepository,
    FakeTokenService,
    FakeUserRepository,
)

NOW = dt.datetime(2026, 9, 30, 12, 0, tzinfo=dt.UTC)


@pytest.fixture
def clock() -> FakeClock:
    return FakeClock(NOW)


@pytest.fixture
def hasher() -> FakePasswordHasher:
    return FakePasswordHasher()


@pytest.fixture
def use_cases(clock: FakeClock, hasher: FakePasswordHasher) -> AuthUseCases:
    return AuthUseCases.build(
        users=FakeUserRepository(),
        refresh_tokens=FakeRefreshTokenRepository(),
        hasher=hasher,
        tokens=FakeTokenService(),
        clock=clock,
    )


def _register(use_cases: AuthUseCases, email: str = "ana@example.com"):
    return use_cases.register.execute(email=email, password="una-clave-larga")


def test_register_normalizes_email_and_returns_a_session(use_cases: AuthUseCases) -> None:
    result = _register(use_cases, email="  Ana@Example.COM ")
    assert result.user.email == "ana@example.com"
    assert result.tokens.access_token and result.tokens.refresh_token
    assert result.tokens.expires_in == 1800


def test_register_rejects_duplicate_email_ignoring_case(use_cases: AuthUseCases) -> None:
    _register(use_cases)
    with pytest.raises(EmailAlreadyRegisteredError):
        _register(use_cases, email="ANA@example.com")


def test_register_validates_email_and_password(use_cases: AuthUseCases) -> None:
    with pytest.raises(DomainValidationError) as bad_email:
        use_cases.register.execute(email="no-es-un-email", password="una-clave-larga")
    assert bad_email.value.field == "email"

    with pytest.raises(DomainValidationError) as short_password:
        use_cases.register.execute(email="ana@example.com", password="corta")
    assert short_password.value.field == "password"


def test_login_succeeds_with_the_right_password(use_cases: AuthUseCases) -> None:
    registered = _register(use_cases)
    result = use_cases.login.execute(email="ANA@example.com", password="una-clave-larga")
    assert result.user.id == registered.user.id


def test_login_failures_share_the_same_error(use_cases: AuthUseCases) -> None:
    _register(use_cases)
    attempts = [
        ("ana@example.com", "otra-clave-larga"),
        ("nadie@example.com", "una-clave-larga"),
        ("esto-no-es-un-email", "una-clave-larga"),
    ]
    messages = set()
    for email, password in attempts:
        with pytest.raises(AuthenticationError) as error:
            use_cases.login.execute(email=email, password=password)
        messages.add(str(error.value))
    assert len(messages) == 1


def test_login_verifies_dummy_hash_when_email_does_not_exist(
    use_cases: AuthUseCases, hasher: FakePasswordHasher
) -> None:
    with pytest.raises(AuthenticationError):
        use_cases.login.execute(email="nadie@example.com", password="una-clave-larga")

    assert hasher.verified_hashes == ["hashed:mimun-dummy-login-password"]


def test_refresh_rotates_the_token(use_cases: AuthUseCases) -> None:
    first = _register(use_cases).tokens
    second = use_cases.refresh.execute(first.refresh_token)
    assert second.refresh_token != first.refresh_token
    third = use_cases.refresh.execute(second.refresh_token)
    assert third.refresh_token != second.refresh_token


def test_reusing_revoked_refresh_token_revokes_all_user_sessions(
    use_cases: AuthUseCases,
) -> None:
    first_session = _register(use_cases).tokens
    second_session = use_cases.login.execute(
        email="ana@example.com", password="una-clave-larga"
    ).tokens
    rotated_session = use_cases.refresh.execute(first_session.refresh_token)

    with pytest.raises(AuthenticationError):
        use_cases.refresh.execute(first_session.refresh_token)

    for refresh_token in (second_session.refresh_token, rotated_session.refresh_token):
        with pytest.raises(AuthenticationError):
            use_cases.refresh.execute(refresh_token)


def test_refresh_rejects_unknown_and_expired_tokens(
    use_cases: AuthUseCases, clock: FakeClock
) -> None:
    with pytest.raises(AuthenticationError):
        use_cases.refresh.execute("no-existe")
    tokens = _register(use_cases).tokens
    clock.advance(dt.timedelta(days=15))
    with pytest.raises(AuthenticationError):
        use_cases.refresh.execute(tokens.refresh_token)


def test_logout_revokes_the_token_and_is_idempotent(use_cases: AuthUseCases) -> None:
    tokens = _register(use_cases).tokens
    use_cases.logout.execute(tokens.refresh_token)
    use_cases.logout.execute(tokens.refresh_token)
    use_cases.logout.execute("desconocido")
    with pytest.raises(AuthenticationError):
        use_cases.refresh.execute(tokens.refresh_token)


def test_authenticate_access_token() -> None:
    authenticator = AuthenticateAccessToken(FakeTokenService())
    user_id = uuid4()
    assert authenticator.execute(f"access-{user_id}") == user_id
    with pytest.raises(AuthenticationError):
        authenticator.execute("basura")
