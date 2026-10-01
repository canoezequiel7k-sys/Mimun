package com.canoezequiel.moodflow.data.local.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


/**
 * [TokenManager]
 * QUÉ HACE: Almacena y recupera de forma segura los tokens de autenticación (Access Token y Refresh Token).
 * POR QUÉ: Evita guardar datos sensibles en texto plano en la memoria del dispositivo.
 * CÓMO FUNCIONA: Utiliza EncryptedSharedPreferences respaldado por la MasterKey del sistema (Android KeyStore)
 *               para encriptar y desencriptar automáticamente la información al leer/escribir.
 */
class TokenManager(context: Context) {
    //Creamos o recuperamos la llave maestra de cifrado respaldada por el KeyStore del dispositivo
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    //Inicializamos el almacenamiento seguro cifrado llaves y valores
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_auth_prefs", //Nombre del archivo de preferencias cifradas
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object{
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    //Guardo ambos tokens cuando el usuario inicia sesion con exito
    fun saveTokens(accessToken: String, refreshToken: String){
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    //Retorna el Access Token actual(Lo usara nuestro AuthInterceptor)
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    //Retorna el Refresh Token (Para renovar el token de acceso cuando expire)
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    //Limpia las credenciales al cerra sesion (Logout)
    fun clearTokens(){
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}