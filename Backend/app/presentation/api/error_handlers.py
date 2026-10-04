import logging

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from slowapi.errors import RateLimitExceeded
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.domain.errors import (
    AuthenticationError,
    DomainValidationError,
    EmailAlreadyRegisteredError,
    MoodEntryAlreadyExistsError,
    NotFoundError,
    RateLimitExceededError,
)

logger = logging.getLogger(__name__)

_HTTP_CODES = {401: "UNAUTHORIZED", 403: "FORBIDDEN", 404: "NOT_FOUND"}


def _error(
    status_code: int,
    code: str,
    message: str,
    details: list[dict[str, str]] | None = None,
    headers: dict[str, str] | None = None,
) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        content={"error": {"code": code, "message": message, "details": details or []}},
        headers=headers,
    )


def register_error_handlers(app: FastAPI) -> None:
    """Traduce errores al formato unificado de `Docs/api/README.md`."""

    @app.exception_handler(DomainValidationError)
    async def _domain_validation(request: Request, exc: DomainValidationError) -> JSONResponse:
        logger.warning(
            "domain_validation_failed",
            extra={
                "method": request.method,
                "path": request.url.path,
                "validation_errors": [
                    {"location": exc.field, "type": "domain_validation", "message": exc.message}
                ],
            },
        )
        details = [{"field": exc.field, "message": exc.message}]
        return _error(422, "VALIDATION_ERROR", "Datos inválidos", details)

    @app.exception_handler(NotFoundError)
    async def _not_found(request: Request, exc: NotFoundError) -> JSONResponse:
        return _error(404, "NOT_FOUND", str(exc))

    @app.exception_handler(MoodEntryAlreadyExistsError)
    async def _mood_exists(request: Request, exc: MoodEntryAlreadyExistsError) -> JSONResponse:
        details = [{"field": "date", "message": str(exc)}]
        return _error(409, "MOOD_ENTRY_ALREADY_EXISTS", str(exc), details)

    @app.exception_handler(EmailAlreadyRegisteredError)
    async def _email_exists(request: Request, exc: EmailAlreadyRegisteredError) -> JSONResponse:
        details = [{"field": "email", "message": str(exc)}]
        return _error(409, "EMAIL_ALREADY_REGISTERED", str(exc), details)

    @app.exception_handler(RateLimitExceeded)
    async def _rate_limit_exceeded(request: Request, exc: RateLimitExceeded) -> JSONResponse:
        response = _error(429, "TOO_MANY_REQUESTS", "Demasiadas peticiones")
        return request.app.state.limiter._inject_headers(
            response, request.state.view_rate_limit
        )

    @app.exception_handler(AuthenticationError)
    async def _authentication(request: Request, exc: AuthenticationError) -> JSONResponse:
        return _error(401, "UNAUTHORIZED", str(exc), headers={"WWW-Authenticate": "Bearer"})

    @app.exception_handler(RateLimitExceededError)
    async def _rate_limited(request: Request, exc: RateLimitExceededError) -> JSONResponse:
        retry_after = {"Retry-After": str(exc.retry_after_seconds)}
        return _error(429, "TOO_MANY_REQUESTS", str(exc), headers=retry_after)

    @app.exception_handler(RequestValidationError)
    async def _request_validation(request: Request, exc: RequestValidationError) -> JSONResponse:
        validation_errors = [
            {
                "location": ".".join(str(part) for part in err["loc"]),
                "type": err["type"],
                "message": err["msg"],
            }
            for err in exc.errors()
        ]
        logger.warning(
            "request_validation_failed",
            extra={
                "method": request.method,
                "path": request.url.path,
                "validation_errors": validation_errors,
            },
        )
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
        return _error(exc.status_code, code, str(exc.detail), headers=exc.headers)

    @app.exception_handler(Exception)
    async def _unhandled(request: Request, exc: Exception) -> JSONResponse:
        logger.error("unhandled_exception", extra={"exception_type": type(exc).__name__})
        return _error(500, "INTERNAL_ERROR", "Error interno del servidor")
