from typing import Protocol


class PasswordHasher(Protocol):
    dummy_hash: str

    def hash(self, plain: str) -> str: ...
    def verify(self, plain: str, hashed: str) -> bool: ...
