import logging
import time
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.container import Container
from app.infrastructure.config import get_settings
from app.infrastructure.structured_logging import configure_logging
from app.presentation.api.error_handlers import register_error_handlers
from app.presentation.api.rate_limit import limiter
from app.presentation.api.router import api_router


def create_app() -> FastAPI:
    settings = get_settings()
    configure_logging()
    container = Container(settings=settings)

    @asynccontextmanager
    async def lifespan(app: FastAPI) -> AsyncIterator[None]:
        app.state.container = container
        yield
        container.dispose()

    is_production = settings.environment == "production"
    app = FastAPI(
        title="Mimun API",
        version="0.1.0",
        lifespan=lifespan,
        docs_url=None if is_production else "/docs",
        redoc_url=None if is_production else "/redoc",
    )
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins,
        allow_credentials=False,
        allow_methods=["GET", "POST", "PUT", "DELETE"],
        allow_headers=["Authorization", "Content-Type"],
    )
    app.state.limiter = limiter
    register_error_handlers(app)
    app.include_router(api_router)

    @app.middleware("http")
    async def log_request(request, call_next):
        started = time.perf_counter()
        response = await call_next(request)
        logger = logging.getLogger("app.http")
        logger.info(
            "http_request",
            extra={
                "method": request.method,
                "path": request.url.path,
                "status_code": response.status_code,
                "duration_ms": round((time.perf_counter() - started) * 1000, 2),
            },
        )
        return response

    return app


app = create_app()
