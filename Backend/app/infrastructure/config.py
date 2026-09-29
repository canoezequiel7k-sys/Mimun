from functools import lru_cache
from typing import Literal

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

_INSECURE_SECRET_KEY = "cambiar-por-un-valor-largo-y-aleatorio"


class Settings(BaseSettings):
    """Configuración de la app, leída de variables de entorno o de `.env`."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    database_url: str = "postgresql+psycopg://mimun:mimun@localhost:5432/mimun"
    test_database_url: str = "postgresql+psycopg://mimun:mimun@localhost:5432/mimun_test"
    secret_key: str = _INSECURE_SECRET_KEY
    access_token_expire_minutes: int = 30
    refresh_token_expire_days: int = 14
    environment: Literal["development", "test", "production"] = "development"

    @model_validator(mode="after")
    def _reject_insecure_secret_in_production(self) -> "Settings":
        if self.environment == "production" and self.secret_key == _INSECURE_SECRET_KEY:
            raise ValueError("SECRET_KEY debe configurarse con un valor propio en producción")
        return self


@lru_cache
def get_settings() -> Settings:
    return Settings()
