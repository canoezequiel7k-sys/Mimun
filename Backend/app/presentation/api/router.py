from fastapi import APIRouter

from app.presentation.api.v1 import health, journal_entries, mood_entries

api_router = APIRouter(prefix="/api/v1")
api_router.include_router(health.router)
api_router.include_router(mood_entries.router)
api_router.include_router(journal_entries.router)
