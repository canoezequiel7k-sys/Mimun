package com.canoezequiel.moodflow.data.remote.auth

import com.canoezequiel.moodflow.data.local.auth.TokenManager
import com.canoezequiel.moodflow.data.local.remote.dto.AuthResponse
import com.canoezequiel.moodflow.data.local.remote.dto.RefreshRequest
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val baseUrl: String
) : Authenticator {

    private val json = Json { ignoreUnknownKeys = true }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Evitamos bucles infinitos si la petición que falló fue de autenticación
        val requestPath = response.request.url.encodedPath
        if (requestPath.contains("auth/refresh") || requestPath.contains("auth/login") || requestPath.contains("auth/register")) {
            return null
        }

        val requestToken = response.request.header("Authorization")?.replace("Bearer ", "")

        synchronized(this) {
            val currentAccessToken = tokenManager.getAccessToken()

            // Si otra petición en paralelo ya renovó el token, reintentamos con el nuevo token
            if (!currentAccessToken.isNullOrBlank() && currentAccessToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val refreshToken = tokenManager.getRefreshToken() ?: run {
                tokenManager.clearTokens()
                return null
            }

            return try {
                val refreshRequestBody = json.encodeToString(
                    RefreshRequest.serializer(),
                    RefreshRequest(refreshToken)
                ).toRequestBody("application/json".toMediaType())

                val refreshRequest = Request.Builder()
                    .url("${baseUrl}auth/refresh")
                    .post(refreshRequestBody)
                    .build()

                // Cliente HTTP simple sin interceptores para realizar la llamada directa de refresh
                val refreshClient = OkHttpClient()
                val refreshResponse = refreshClient.newCall(refreshRequest).execute()

                if (refreshResponse.isSuccessful) {
                    val responseBodyString = refreshResponse.body?.string() ?: ""
                    val authResponse = json.decodeFromString(
                        AuthResponse.serializer(),
                        responseBodyString
                    )

                    // Guardamos el nuevo par de tokens (el refresh token rotó)
                    tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)

                    // Reintentamos la petición original con el nuevo access_token
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${authResponse.accessToken}")
                        .build()
                } else {
                    // Si el refresh falló: borramos tokens para redirigir al login
                    tokenManager.clearTokens()
                    null
                }
            } catch (e: Exception) {
                tokenManager.clearTokens()
                null
            }
        }
    }
}