import datetime as dt
from typing import Protocol
from uuid import UUID

from app.domain.entities import RefreshToken


class RefreshTokenRepository(Protocol):
    def add(self, token: RefreshToken) -> None: ...
    def get_by_hash(self, token_hash: str) -> RefreshToken | None: ...

    def revoke(self, token_id: UUID, now: dt.datetime) -> bool:
        """True si lo revocó; False si ya estaba revocado (protege de usos concurrentes)."""
        ...
