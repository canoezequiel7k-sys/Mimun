import datetime as dt
from dataclasses import dataclass
from uuid import UUID


@dataclass(slots=True)
class RefreshToken:
    id: UUID
    user_id: UUID
    token_hash: str
    expires_at: dt.datetime
    created_at: dt.datetime
    revoked_at: dt.datetime | None = None

    def is_usable(self, now: dt.datetime) -> bool:
        return self.revoked_at is None and self.expires_at > now
