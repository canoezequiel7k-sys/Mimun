package com.canoezequiel.moodflow.data.remote.api

import com.canoezequiel.moodflow.MoodApplication
import com.canoezequiel.moodflow.data.local.auth.TokenManager
import com.canoezequiel.moodflow.data.local.remote.api.ApiService
import com.canoezequiel.moodflow.data.remote.auth.AuthInterceptor
import com.canoezequiel.moodflow.data.remote.auth.TokenAuthenticator
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.getValue
import kotlin.jvm.java

object ApiClient {
    //URL para emulador Android local (equivale a localhost de tu PC)
    private const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
    //Instancia de nuestro TokenManager usando el contexto de la aplicacion
    private val tokenManager = TokenManager(MoodApplication.context)

    //Configuramos JSON para ignorar campos nuevos que mande el backend que la app aún no conozca
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    //Interceptor para ver las peticiones y respuestas HTTP en el Logcat (muy útil para estudiar)
    private val okHttpClient = OkHttpClient.Builder()
        //Inyectamos el token Bearer en cada peticion saliendo
        .addInterceptor(AuthInterceptor { tokenManager.getAccessToken() })
        .authenticator(TokenAuthenticator(tokenManager, BASE_URL))
        //Registra los logs de red en consola para depuracion (Solo debug)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
        })
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}