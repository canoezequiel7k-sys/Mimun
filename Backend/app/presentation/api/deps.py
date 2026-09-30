from collections.abc import Iterator
from uuid import UUID

from fastapi import Request

from app.application.use_cases.mood_entries import MoodEntryUseCases


def get_current_user_id(request: Request) -> UUID:
    """B3: usuario fijo de desarrollo. En B6 se reemplaza por el usuario del token."""
    return request.app.state.container.dev_user_id()


def get_mood_entry_use_cases(request: Request) -> Iterator[MoodEntryUseCases]:
    with request.app.state.container.mood_entry_use_cases() as use_cases:
        yield use_cases
