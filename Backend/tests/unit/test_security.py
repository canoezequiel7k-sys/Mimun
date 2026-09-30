import jwt
import pytest

from app.domain.errors import AuthenticationError
from app.infrastructure.security.jwt_token_service import JwtTokenService
from app.infrastructure.security.password_hasher import Argon2PasswordHasher

SECRET = "s" * 40


def _service(minutes: int = 30, secret: str = SECRET) -> JwtTokenService:
    return JwtTokenService(secret_key=secret, access_token_minutes=minutes, refresh_token_days=14)


def test_access_token_roundtrip() -> None:
    from uuid import uuid4

    service = _service()
    user_id = uuid4()
    assert service.read_access_token(service.create_access_token(user_id)) == user_id
    assert service.access_token_expires_in == 1800


def test_access_token_rejects_wrong_secret_expired_garbage_and_wrong_type() -> None:
    from uuid import uuid4

    token = _service().create_access_token(uuid4())
    with pytest.raises(AuthenticationError):
        _service(secret="otra-clave-distinta-de-mas-de-32-bytes").read_access_token(token)
    with pytest.raises(AuthenticationError):
        _service(minutes=-1).read_access_token(_service(minutes=-1).create_access_token(uuid4()))
    with pytest.raises(AuthenticationError):
        _service().read_access_token("esto-no-es-un-jwt")

    refresh_like = jwt.encode({"sub": str(uuid4()), "type": "refresh", "exp": 9999999999}, SECRET)
    with pytest.raises(AuthenticationError):
        _service().read_access_token(refresh_like)


def test_refresh_tokens_are_unique_and_hash_is_deterministic() -> None:
    service = _service()
    first, second = service.new_refresh_token(), service.new_refresh_token()
    assert first != second
    assert service.hash_refresh_token(first) == service.hash_refresh_token(first)
    assert len(service.hash_refresh_token(first)) == 64
    assert service.hash_refresh_token(first) != first


def test_argon2_hasher_verifies_and_never_raises() -> None:
    hasher = Argon2PasswordHasher()
    hashed = hasher.hash("una-clave-larga")
    assert hashed != "una-clave-larga"
    assert hasher.verify("una-clave-larga", hashed) is True
    assert hasher.verify("otra-clave-larga", hashed) is False
    assert hasher.verify("una-clave-larga", "no-es-un-hash") is False
