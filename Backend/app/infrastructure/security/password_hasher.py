from argon2 import PasswordHasher
from argon2.exceptions import InvalidHashError, VerificationError


class Argon2PasswordHasher:
    def __init__(self) -> None:
        self._hasher = PasswordHasher()

    def hash(self, plain: str) -> str:
        return self._hasher.hash(plain)

    def verify(self, plain: str, hashed: str) -> bool:
        try:
            return self._hasher.verify(hashed, plain)
        except (VerificationError, InvalidHashError):
            return False
