import datetime as dt
from typing import Protocol
from uuid import UUID


class TokenService(Protocol):
    @property
    def access_token_expires_in(self) -> int:
        """Segundos de vida del access token."""
        ...

    @property
    def refresh_token_ttl(self) -> dt.timedelta: ...

    def create_access_token(self, user_id: UUID) -> str: ...

    def read_access_token(self, token: str) -> UUID:
        """Lanza AuthenticationError si es inválido o venció."""
        ...

    def new_refresh_token(self) -> str: ...
    def hash_refresh_token(self, token: str) -> str: ...
