package com.canoezequiel.moodflow.domain.repository

import com.canoezequiel.moodflow.data.local.remote.dto.AuthResponse

//Contrato de dominio para la autenticacion
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(email: String, password: String): Result<AuthResponse>
    fun logout()
    fun isLoggedIn(): Boolean
}