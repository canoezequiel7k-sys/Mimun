from uuid import UUID

from pydantic import BaseModel, ConfigDict


class CredentialsRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    email: str
    password: str


class RefreshTokenRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    refresh_token: str


class UserResponse(BaseModel):
    id: UUID
    email: str


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int


class AuthResponse(TokenResponse):
    user: UserResponse
