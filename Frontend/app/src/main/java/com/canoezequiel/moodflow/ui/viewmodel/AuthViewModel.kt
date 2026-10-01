package com.canoezequiel.moodflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canoezequiel.moodflow.MoodApplication
import com.canoezequiel.moodflow.data.local.auth.TokenManager
import com.canoezequiel.moodflow.data.remote.api.ApiClient
import com.canoezequiel.moodflow.data.repository.AuthRepositoryImpl
import com.canoezequiel.moodflow.domain.usecase.LoginUseCase
import com.canoezequiel.moodflow.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado inmutable de la UI para autenticación
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

/**
 * [AuthViewModel]
 * QUÉ HACE: Administra el estado de la autenticación y procesa las acciones de Login y Registro.
 * POR QUÉ: Sigue MVVM separando la lógica de presentación de la UI.
 * CÓMO FUNCIONA: Utiliza corrutinas para ejecutar llamadas de red asíncronas y actualiza el StateFlow
 *               con estados de carga o error de forma reactiva.
 */
class AuthViewModel : ViewModel() {

    // Inicializamos el repositorio y casos de uso de autenticación
    private val authRepository = AuthRepositoryImpl(
        ApiClient.apiService,
        TokenManager(MoodApplication.context)
    )
    private val loginUseCase = LoginUseCase(authRepository)
    private val registerUseCase = RegisterUseCase(authRepository)

    // Estado expuesto de forma inmutable a la UI
    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = authRepository.isLoggedIn())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    // Acción de Iniciar Sesión
    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginUseCase(state.email, state.password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Error al iniciar sesión") }
                }
            )
        }
    }

    // Acción de Registrarse
    fun register() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = registerUseCase(state.email, state.password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Error al registrarse") }
                }
            )
        }
    }

    // Acción de Cerrar Sesión
    fun logout() {
        authRepository.logout()
        _uiState.update { AuthUiState(isAuthenticated = false) }
    }
}