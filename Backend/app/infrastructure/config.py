from functools import lru_cache
from typing import Literal

from pydantic import Field, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

_INSECURE_SECRET_KEY = "cambiar-por-un-valor-largo-y-aleatorio"


class Settings(BaseSettings):
    """Configuración de la app, leída de variables de entorno o de `.env`."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    database_url: str = "postgresql+psycopg://mimun:mimun@localhost:5432/mimun"
    test_database_url: str = "postgresql+psycopg://mimun:mimun@localhost:5432/mimun_test"
    secret_key: str = _INSECURE_SECRET_KEY
    access_token_expire_minutes: int = Field(default=30, gt=0)
    refresh_token_expire_days: int = Field(default=14, gt=0)
    auth_enabled: bool = True
    # Intentos permitidos por IP en /auth/register, /login y /refresh dentro de la ventana.
    # 0 desactiva el límite.
    auth_rate_limit_attempts: int = 10
    auth_rate_limit_window_seconds: int = 60
    # Orígenes web permitidos (CORS), separados por coma. Vacío = sin CORS.
    # La app Android no lo necesita: solo un cliente web.
    cors_origins: str = ""
    environment: Literal["development", "test", "production"] = "development"

    @property
    def cors_origin_list(self) -> list[str]:
        return [origin.strip() for origin in self.cors_origins.split(",") if origin.strip()]

    @model_validator(mode="after")
    def _validate_production(self) -> "Settings":
        if self.environment == "production":
            if self.secret_key == _INSECURE_SECRET_KEY or len(self.secret_key) < 32:
                raise ValueError("SECRET_KEY debe ser propia y de al menos 32 caracteres")
            if not self.auth_enabled:
                raise ValueError("AUTH_ENABLED debe ser true en producción")
            if "*" in self.cors_origin_list:
                raise ValueError("CORS_ORIGINS no puede ser '*' en producción")
        return self


@lru_cache
def get_settings() -> Settings:
    return Settings()
