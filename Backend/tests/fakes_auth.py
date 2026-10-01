import datetime as dt
from uuid import UUID

from app.domain.entities import RefreshToken, User
from app.domain.errors import AuthenticationError, EmailAlreadyRegisteredError


class FakePasswordHasher:
    dummy_hash = "hashed:mimun-dummy-login-password"

    def __init__(self) -> None:
        self.verified_hashes: list[str] = []

    def hash(self, plain: str) -> str:
        return f"hashed:{plain}"

    def verify(self, plain: str, hashed: str) -> bool:
        self.verified_hashes.append(hashed)
        return hashed == f"hashed:{plain}"


class FakeTokenService:
    access_token_expires_in = 1800
    refresh_token_ttl = dt.timedelta(days=14)

    def __init__(self) -> None:
        self._counter = 0

    def create_access_token(self, user_id: UUID) -> str:
        return f"access-{user_id}"

    def read_access_token(self, token: str) -> UUID:
        if not token.startswith("access-"):
            raise AuthenticationError("Token inválido o vencido")
        return UUID(token.removeprefix("access-"))

    def new_refresh_token(self) -> str:
        self._counter += 1
        return f"refresh-{self._counter}"

    def hash_refresh_token(self, token: str) -> str:
        return f"hash-{token}"


class FakeUserRepository:
    def __init__(self) -> None:
        self.items: dict[UUID, User] = {}

    def add(self, user: User) -> None:
        if self.get_by_email(user.email) is not None:
            raise EmailAlreadyRegisteredError()
        self.items[user.id] = user

    def get_by_id(self, user_id: UUID) -> User | None:
        return self.items.get(user_id)

    def get_by_email(self, email: str) -> User | None:
        return next((u for u in self.items.values() if u.email == email.lower()), None)


class FakeRefreshTokenRepository:
    def __init__(self) -> None:
        self.items: dict[UUID, RefreshToken] = {}

    def add(self, token: RefreshToken) -> None:
        self.items[token.id] = token

    def get_by_hash(self, token_hash: str) -> RefreshToken | None:
        return next((t for t in self.items.values() if t.token_hash == token_hash), None)

    def revoke(self, token_id: UUID, now: dt.datetime) -> bool:
        token = self.items[token_id]
        if token.revoked_at is not None:
            return False
        token.revoked_at = now
        return True

    def revoke_all_for_user(self, user_id: UUID, now: dt.datetime) -> None:
        for token in self.items.values():
            if token.user_id == user_id and token.revoked_at is None:
                token.revoked_at = now
