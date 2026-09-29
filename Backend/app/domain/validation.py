"""Normalización y validación de textos, compartida por las entidades."""

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
