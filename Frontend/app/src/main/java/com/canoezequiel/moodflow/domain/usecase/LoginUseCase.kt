package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.data.local.remote.dto.AuthResponse
import com.canoezequiel.moodflow.domain.repository.AuthRepository


/**
 * [LoginUseCase]
 * QUÉ HACE: Caso de uso para iniciar sesión.
 * POR QUÉ: Aisla la lógica de negocio del login para que los ViewModels no accedan directamente al repositorio.
 * CÓMO FUNCIONA: Invoca al método login del AuthRepository devolviendo un Result con la respuesta de autenticación.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResponse> {
        return authRepository.login(email, password)
    }
}