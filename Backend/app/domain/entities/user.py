import datetime as dt
from dataclasses import dataclass
from uuid import UUID


@dataclass(slots=True)
class User:
    id: UUID
    email: str
    password_hash: str
    created_at: dt.datetime
    updated_at: dt.datetime
