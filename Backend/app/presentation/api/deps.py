from collections.abc import Iterator
from typing import Annotated
from uuid import UUID

from fastapi import Depends, Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.application.use_cases.auth import AuthUseCases
from app.application.use_cases.journal_entries import JournalEntryUseCases
from app.application.use_cases.mood_entries import MoodEntryUseCases
from app.domain.errors import AuthenticationError

_bearer = HTTPBearer(auto_error=False)


def get_current_user_id(
    request: Request,
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(_bearer)],
) -> UUID:
    container = request.app.state.container
    if not container.settings.auth_enabled:
        return container.dev_user_id()
    if credentials is None:
        raise AuthenticationError("Falta el token de acceso")
    return container.authenticator.execute(credentials.credentials)


def get_mood_entry_use_cases(request: Request) -> Iterator[MoodEntryUseCases]:
    with request.app.state.container.mood_entry_use_cases() as use_cases:
        yield use_cases


def get_journal_entry_use_cases(request: Request) -> Iterator[JournalEntryUseCases]:
    with request.app.state.container.journal_entry_use_cases() as use_cases:
        yield use_cases


def get_auth_use_cases(request: Request) -> Iterator[AuthUseCases]:
    with request.app.state.container.auth_use_cases() as use_cases:
        yield use_cases


def enforce_auth_rate_limit(request: Request) -> None:
    """Limita intentos por IP en los endpoints de autenticación.

    Detrás de un proxy inverso hay que usar la IP real del cliente (ver Fase B9).
    """
    client_ip = request.client.host if request.client else "desconocido"
    request.app.state.container.auth_rate_limiter.check(client_ip)
