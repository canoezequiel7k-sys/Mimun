import datetime as dt
import hashlib
import secrets
from uuid import UUID

import jwt

from app.domain.errors import AuthenticationError

_ALGORITHM = "HS256"
_INVALID_TOKEN = "Token inválido o vencido"


class JwtTokenService:
    def __init__(
        self, *, secret_key: str, access_token_minutes: int, refresh_token_days: int
    ) -> None:
        self._secret_key = secret_key
        self._access_token_minutes = access_token_minutes
        self._refresh_token_days = refresh_token_days

    @property
    def access_token_expires_in(self) -> int:
        return self._access_token_minutes * 60

    @property
    def refresh_token_ttl(self) -> dt.timedelta:
        return dt.timedelta(days=self._refresh_token_days)

    def create_access_token(self, user_id: UUID) -> str:
        now = dt.datetime.now(dt.UTC)
        payload = {
            "sub": str(user_id),
            "type": "access",
            "iat": now,
            "exp": now + dt.timedelta(minutes=self._access_token_minutes),
        }
        return jwt.encode(payload, self._secret_key, algorithm=_ALGORITHM)

    def read_access_token(self, token: str) -> UUID:
        try:
            payload = jwt.decode(
                token,
                self._secret_key,
                algorithms=[_ALGORITHM],
                options={"require": ["exp", "sub"]},
                leeway=10,
            )
            user_id = UUID(payload["sub"])
        except (jwt.PyJWTError, ValueError) as exc:
            raise AuthenticationError(_INVALID_TOKEN) from exc
        if payload.get("type") != "access":
            raise AuthenticationError(_INVALID_TOKEN)
        return user_id

    def new_refresh_token(self) -> str:
        return secrets.token_urlsafe(48)

    def hash_refresh_token(self, token: str) -> str:
        return hashlib.sha256(token.encode()).hexdigest()
