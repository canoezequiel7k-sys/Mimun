from typing import Annotated

from fastapi import APIRouter, Depends

from app.application.use_cases.auth import AuthResult, AuthUseCases, TokenPair
from app.presentation.api.deps import enforce_auth_rate_limit, get_auth_use_cases
from app.presentation.api.schemas.auth import (
    AuthResponse,
    CredentialsRequest,
    RefreshTokenRequest,
    TokenResponse,
    UserResponse,
)

router = APIRouter(prefix="/auth", tags=["auth"])

UseCases = Annotated[AuthUseCases, Depends(get_auth_use_cases)]
# Se aplica a los endpoints que un atacante puede usar para probar credenciales o tokens.
_rate_limited = [Depends(enforce_auth_rate_limit)]


def _token_response(tokens: TokenPair) -> TokenResponse:
    return TokenResponse(
        access_token=tokens.access_token,
        refresh_token=tokens.refresh_token,
        expires_in=tokens.expires_in,
    )


def _auth_response(result: AuthResult) -> AuthResponse:
    return AuthResponse(
        user=UserResponse(id=result.user.id, email=result.user.email),
        access_token=result.tokens.access_token,
        refresh_token=result.tokens.refresh_token,
        expires_in=result.tokens.expires_in,
    )


@router.post("/register", response_model=AuthResponse, status_code=201, dependencies=_rate_limited)
def register(body: CredentialsRequest, use_cases: UseCases) -> AuthResponse:
    return _auth_response(use_cases.register.execute(email=body.email, password=body.password))


@router.post("/login", response_model=AuthResponse, dependencies=_rate_limited)
def login(body: CredentialsRequest, use_cases: UseCases) -> AuthResponse:
    return _auth_response(use_cases.login.execute(email=body.email, password=body.password))


@router.post("/refresh", response_model=TokenResponse, dependencies=_rate_limited)
def refresh(body: RefreshTokenRequest, use_cases: UseCases) -> TokenResponse:
    return _token_response(use_cases.refresh.execute(body.refresh_token))


@router.post("/logout", status_code=204)
def logout(body: RefreshTokenRequest, use_cases: UseCases) -> None:
    use_cases.logout.execute(body.refresh_token)
