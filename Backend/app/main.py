from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.container import Container
from app.infrastructure.config import get_settings
from app.presentation.api.router import api_router


def create_app() -> FastAPI:
    settings = get_settings()
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
    app.include_router(api_router)
    return app


app = create_app()
