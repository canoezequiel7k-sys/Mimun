from dataclasses import dataclass
from uuid import UUID, uuid4

from app.application.ports.clock import Clock
from app.application.ports.password_hasher import PasswordHasher
from app.application.ports.token_service import TokenService
from app.domain.entities import RefreshToken, User
from app.domain.errors import (
    AuthenticationError,
    DomainValidationError,
    EmailAlreadyRegisteredError,
)
from app.domain.repositories import RefreshTokenRepository, UserRepository
from app.domain.validation import normalize_email, validate_password

_INVALID_SESSION = "Sesión inválida o vencida"


@dataclass(frozen=True, slots=True)
class TokenPair:
    access_token: str
    refresh_token: str
    expires_in: int


@dataclass(frozen=True, slots=True)
class AuthResult:
    user: User
    tokens: TokenPair


class SessionIssuer:
    """Crea un par de tokens y guarda el hash del refresh token."""

    def __init__(
        self, refresh_tokens: RefreshTokenRepository, tokens: TokenService, clock: Clock
    ) -> None:
        self._refresh_tokens = refresh_tokens
        self._tokens = tokens
        self._clock = clock

    def issue(self, user_id: UUID) -> TokenPair:
        now = self._clock.now()
        raw_refresh_token = self._tokens.new_refresh_token()
        self._refresh_tokens.add(
            RefreshToken(
                id=uuid4(),
                user_id=user_id,
                token_hash=self._tokens.hash_refresh_token(raw_refresh_token),
                expires_at=now + self._tokens.refresh_token_ttl,
                created_at=now,
            )
        )
        return TokenPair(
            access_token=self._tokens.create_access_token(user_id),
            refresh_token=raw_refresh_token,
            expires_in=self._tokens.access_token_expires_in,
        )


class RegisterUser:
    def __init__(
        self, users: UserRepository, hasher: PasswordHasher, issuer: SessionIssuer, clock: Clock
    ) -> None:
        self._users = users
        self._hasher = hasher
        self._issuer = issuer
        self._clock = clock

    def execute(self, *, email: str, password: str) -> AuthResult:
        normalized_email = normalize_email(email)
        validate_password(password)
        if self._users.get_by_email(normalized_email) is not None:
            raise EmailAlreadyRegisteredError()
        now = self._clock.now()
        user = User(
            id=uuid4(),
            email=normalized_email,
            password_hash=self._hasher.hash(password),
            created_at=now,
            updated_at=now,
        )
        self._users.add(user)
        return AuthResult(user=user, tokens=self._issuer.issue(user.id))


class LoginUser:
    def __init__(
        self, users: UserRepository, hasher: PasswordHasher, issuer: SessionIssuer
    ) -> None:
        self._users = users
        self._hasher = hasher
        self._issuer = issuer

    def execute(self, *, email: str, password: str) -> AuthResult:
        try:
            normalized_email = normalize_email(email)
        except DomainValidationError as exc:
            raise AuthenticationError() from exc
        user = self._users.get_by_email(normalized_email)
        if user is None:
            self._hasher.verify(password, self._hasher.dummy_hash)
            raise AuthenticationError()
        if not self._hasher.verify(password, user.password_hash):
            raise AuthenticationError()
        return AuthResult(user=user, tokens=self._issuer.issue(user.id))


class RefreshSession:
    def __init__(
        self,
        refresh_tokens: RefreshTokenRepository,
        tokens: TokenService,
        issuer: SessionIssuer,
        clock: Clock,
    ) -> None:
        self._refresh_tokens = refresh_tokens
        self._tokens = tokens
        self._issuer = issuer
        self._clock = clock

    def execute(self, refresh_token: str) -> TokenPair:
        now = self._clock.now()
        stored = self._refresh_tokens.get_by_hash(self._tokens.hash_refresh_token(refresh_token))
        if stored is None:
            raise AuthenticationError(_INVALID_SESSION)
        if stored.revoked_at is not None:
            self._refresh_tokens.revoke_all_for_user(stored.user_id, now)
            raise AuthenticationError(_INVALID_SESSION)
        if not stored.is_usable(now):
            raise AuthenticationError(_INVALID_SESSION)
        if not self._refresh_tokens.revoke(stored.id, now):
            self._refresh_tokens.revoke_all_for_user(stored.user_id, now)
            raise AuthenticationError(_INVALID_SESSION)
        return self._issuer.issue(stored.user_id)


class LogoutUser:
    def __init__(
        self, refresh_tokens: RefreshTokenRepository, tokens: TokenService, clock: Clock
    ) -> None:
        self._refresh_tokens = refresh_tokens
        self._tokens = tokens
        self._clock = clock

    def execute(self, refresh_token: str) -> None:
        """Idempotente: un token desconocido o ya revocado no es un error."""
        stored = self._refresh_tokens.get_by_hash(self._tokens.hash_refresh_token(refresh_token))
        if stored is not None and stored.revoked_at is None:
            self._refresh_tokens.revoke(stored.id, self._clock.now())


class AuthenticateAccessToken:
    def __init__(self, tokens: TokenService) -> None:
        self._tokens = tokens

    def execute(self, access_token: str) -> UUID:
        return self._tokens.read_access_token(access_token)


@dataclass(frozen=True, slots=True)
class AuthUseCases:
    register: RegisterUser
    login: LoginUser
    refresh: RefreshSession
    logout: LogoutUser

    @classmethod
    def build(
        cls,
        *,
        users: UserRepository,
        refresh_tokens: RefreshTokenRepository,
        hasher: PasswordHasher,
        tokens: TokenService,
        clock: Clock,
    ) -> "AuthUseCases":
        issuer = SessionIssuer(refresh_tokens, tokens, clock)
        return cls(
            register=RegisterUser(users, hasher, issuer, clock),
            login=LoginUser(users, hasher, issuer),
            refresh=RefreshSession(refresh_tokens, tokens, issuer, clock),
            logout=LogoutUser(refresh_tokens, tokens, clock),
        )
