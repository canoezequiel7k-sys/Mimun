package com.canoezequiel.moodflow.data.remote.auth

import okhttp3.Interceptor
import okhttp3.Response


/**
 * [AuthInterceptor]
 * QUÉ HACE: Intercepta cada petición HTTP saliente para añadir el token de autenticación (Bearer Token)
 *          en los headers, y maneja respuestas de sesión expirada (401).
 * POR QUÉ: Evita repetir código en cada llamada a la API y centraliza la seguridad de la sesión.
 * CÓMO FUNCIONA: Lee el token almacenado, lo inyecta en la cabecera "Authorization",
 *               y vigila si el servidor responde con 401 (sesión expirada).
 */
class AuthInterceptor (
    private val tokenProvider: () -> String? //Proveedor que obtiene el token actual guardado de forma segura
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //Obtenemos la peticion original que se iba a enviar al servidor
        val originalRequest = chain.request()

        //Consultamos si tenemos un token disponible
        val token = tokenProvider()

        //Si el token existe, clonamos la peticion y le agregamos el header "Authorization"
        val requestWithAuth = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }else {
            //Si no hay token (ej. pantalla de login), mandamos la peticion tal cual
            originalRequest
        }

        //Continuamos con la ejecucion de la red (Enviamos la peticion)
        val response = chain.proceed(requestWithAuth)

        //Control de sesion expirado (Codigo 401 Unauthorized por parte del backend)
        if (response.code == 401){
            //Aqui agregamos la logica para refrescar el token o expulsar el usuario en el futuro
        }

        return response
    }
}