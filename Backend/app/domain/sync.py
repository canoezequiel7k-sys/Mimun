"""Reglas de sincronización sobre los instantes que informa el cliente."""

import datetime as dt

from app.domain.errors import DomainValidationError


def require_aware(value: dt.datetime, field: str) -> dt.datetime:
    if value.tzinfo is None:
        raise DomainValidationError(field, "Debe incluir zona horaria (ej.: 2026-09-30T10:00:00Z)")
    return value


def resolve_client_instant(
    value: dt.datetime | None, now: dt.datetime, *, field: str
) -> dt.datetime:
    """Instante informado por el cliente, acotado al presente.

    Un reloj adelantado no puede ganar todos los conflictos: nunca supera la hora del servidor.
    Si el cliente no lo informa, vale la hora del servidor.
    """
    if value is None:
        return now
    return min(require_aware(value, field), now)
