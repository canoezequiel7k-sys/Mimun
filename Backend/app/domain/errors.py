"""Errores de dominio.

No conocen HTTP. La capa `presentation` los traduce a códigos de estado y al
formato de error unificado (ver `Docs/api/README.md`).
"""

import datetime as dt


class DomainError(Exception):
    """Base de todos los errores de dominio."""


class DomainValidationError(DomainError):
    """Un dato viola una regla del dominio (largo máximo, campo vacío...)."""

    def __init__(self, field: str, message: str) -> None:
        super().__init__(f"{field}: {message}")
        self.field = field
        self.message = message


class NotFoundError(DomainError):
    """El recurso no existe o pertenece a otro usuario (ver decisión 0007)."""

    def __init__(self, resource: str) -> None:
        super().__init__(f"{resource} no encontrado")
        self.resource = resource


class MoodEntryAlreadyExistsError(DomainError):
    """Ya existe un registro emocional para ese usuario en esa fecha."""

    def __init__(self, date: dt.date) -> None:
        super().__init__(f"Ya existe un registro emocional para {date.isoformat()}")
        self.date = date
