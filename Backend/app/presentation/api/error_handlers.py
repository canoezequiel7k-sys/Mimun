import logging

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.domain.errors import DomainValidationError, MoodEntryAlreadyExistsError, NotFoundError

logger = logging.getLogger(__name__)

_HTTP_CODES = {401: "UNAUTHORIZED", 403: "FORBIDDEN", 404: "NOT_FOUND"}


def _error(
    status_code: int, code: str, message: str, details: list[dict[str, str]] | None = None
) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        content={"error": {"code": code, "message": message, "details": details or []}},
    )


def register_error_handlers(app: FastAPI) -> None:
    """Traduce errores al formato unificado de `Docs/api/README.md`."""

    @app.exception_handler(DomainValidationError)
    async def _domain_validation(request: Request, exc: DomainValidationError) -> JSONResponse:
        details = [{"field": exc.field, "message": exc.message}]
        return _error(422, "VALIDATION_ERROR", "Datos inválidos", details)

    @app.exception_handler(NotFoundError)
    async def _not_found(request: Request, exc: NotFoundError) -> JSONResponse:
        return _error(404, "NOT_FOUND", str(exc))

    @app.exception_handler(MoodEntryAlreadyExistsError)
    async def _mood_exists(request: Request, exc: MoodEntryAlreadyExistsError) -> JSONResponse:
        details = [{"field": "date", "message": str(exc)}]
        return _error(409, "MOOD_ENTRY_ALREADY_EXISTS", str(exc), details)

    @app.exception_handler(RequestValidationError)
    async def _request_validation(request: Request, exc: RequestValidationError) -> JSONResponse:
        details = []
        for err in exc.errors():
            loc = err["loc"]
            if err["type"] == "json_invalid":
                field = "body"
            else:
                field = ".".join(str(part) for part in loc[1:]) or str(loc[0])
            details.append({"field": field, "message": err["msg"]})
        return _error(422, "VALIDATION_ERROR", "Datos inválidos", details)

    @app.exception_handler(StarletteHTTPException)
    async def _http_exception(request: Request, exc: StarletteHTTPException) -> JSONResponse:
        code = _HTTP_CODES.get(exc.status_code, f"HTTP_{exc.status_code}")
        return _error(exc.status_code, code, str(exc.detail))

    @app.exception_handler(Exception)
    async def _unhandled(request: Request, exc: Exception) -> JSONResponse:
        logger.exception("Error no controlado")
        return _error(500, "INTERNAL_ERROR", "Error interno del servidor")
