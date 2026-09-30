from sqlalchemy.exc import IntegrityError


def violated_constraint(exc: IntegrityError) -> str | None:
    """Nombre del constraint que PostgreSQL reporta como violado."""
    diag = getattr(exc.orig, "diag", None)
    return getattr(diag, "constraint_name", None)
