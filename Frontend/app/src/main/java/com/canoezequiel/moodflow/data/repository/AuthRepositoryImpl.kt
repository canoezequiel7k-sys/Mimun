package com.canoezequiel.moodflow.data.repository

import com.canoezequiel.moodflow.data.local.auth.TokenManager
import com.canoezequiel.moodflow.data.local.remote.api.ApiService
import com.canoezequiel.moodflow.data.local.remote.dto.AuthResponse
import com.canoezequiel.moodflow.data.local.remote.dto.LoginRequest
import com.canoezequiel.moodflow.data.local.remote.dto.RefreshRequest
import com.canoezequiel.moodflow.data.local.remote.dto.RegisterRequest
import com.canoezequiel.moodflow.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * [AuthRepositoryImpl]
 * QUÉ HACE: Implementa el contrato de autenticación conectando la API de Retrofit con el TokenManager cifrado.
 * POR QUÉ: Mantiene separada la lógica de infraestructura (red y almacenamiento) del resto de la app.
 * CÓMO FUNCIONA: Realiza el login/registro, captura la respuesta, guarda los tokens de manera segura
 *               y maneja el estado de sesión del usuario.
 */
class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
): AuthRepository {


    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            // Si el login es exitoso, guardamos los tokens en el almacenamiento cifrado
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(email, password))
            // Si el registro es exitoso, guardamos los tokens automáticamente
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    apiService.logout(RefreshRequest(refreshToken))
                } catch (_: Exception) {
                    // Ignoramos fallos de red al cerrar sesión para garantizar el borrado local
                }
            }
        }
        tokenManager.clearTokens()
    }

    override fun isLoggedIn(): Boolean {
        // Verifica si hay un Access Token válido guardado
        return !tokenManager.getAccessToken().isNullOrBlank()
    }
}