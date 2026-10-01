package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.data.local.remote.dto.AuthResponse
import com.canoezequiel.moodflow.domain.repository.AuthRepository


/**
 * [RegisterUseCase]
 * QUÉ HACE: Caso de uso para registrar un nuevo usuario.
 * POR QUÉ: Centraliza la regla de negocio del registro de cuentas.
 * CÓMO FUNCIONA: Invoca al método register del AuthRepository.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResponse>{
        return authRepository.register(email, password)
    }
}