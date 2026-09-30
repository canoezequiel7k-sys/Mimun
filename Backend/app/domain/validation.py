"""Normalización y validación de textos, compartida por las entidades."""

import re

from app.domain.errors import DomainValidationError


def normalize_optional_text(value: str | None, *, field: str, max_length: int) -> str | None:
    """Recorta espacios. Si queda vacío devuelve `None`. Falla si supera `max_length`."""
    if value is None:
        return None
    text = value.strip()
    if not text:
        return None
    _check_max_length(text, field=field, max_length=max_length)
    return text


def normalize_required_text(value: str, *, field: str, max_length: int) -> str:
    """Recorta espacios. Falla si queda vacío o supera `max_length`."""
    text = value.strip()
    if not text:
        raise DomainValidationError(field, "No puede estar vacío")
    _check_max_length(text, field=field, max_length=max_length)
    return text


def _check_max_length(text: str, *, field: str, max_length: int) -> None:
    if len(text) > max_length:
        raise DomainValidationError(field, f"Máximo {max_length} caracteres")


_EMAIL_PATTERN = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")
MAX_EMAIL_LENGTH = 254
MIN_PASSWORD_LENGTH = 8
MAX_PASSWORD_LENGTH = 128


def normalize_email(value: str) -> str:
    email = value.strip().lower()
    if len(email) > MAX_EMAIL_LENGTH or not _EMAIL_PATTERN.match(email):
        raise DomainValidationError("email", "Email inválido")
    return email


def validate_password(value: str) -> None:
    if not MIN_PASSWORD_LENGTH <= len(value) <= MAX_PASSWORD_LENGTH:
        raise DomainValidationError(
            "password",
            f"Debe tener entre {MIN_PASSWORD_LENGTH} y {MAX_PASSWORD_LENGTH} caracteres",
        )
