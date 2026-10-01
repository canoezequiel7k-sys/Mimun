package com.canoezequiel.moodflow.data.local.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


//Dato que enviamos al hacer el Login
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

//Datos que enviamos al hacer el registro
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

//Respuesta del servidor al autenticar con exito (contiene los tokens)
@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer"
)