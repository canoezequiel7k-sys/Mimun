package com.canoezequiel.moodflow.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.ui.viewmodel.AuthViewModel

/**
 * [AuthContainer] (El Coordinador)
 * QUÉ HACE: Controla qué pantalla mostrar (Login o Register) dentro del flujo de autenticación.
 * POR QUÉ: Desacopla las pantallas individuales y centraliza la lógica de navegación de auth.
 * CÓMO FUNCIONA: Usa una variable de estado booleana local para alternar entre Login y Register,
 *               compartiendo el mismo AuthViewModel para ambas vistas.
 */


@Composable
fun AuthContainer(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    //Estado local para saber si el usuario esta en la vista de registro(true) o login (false)
    var isRegistering by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    //Si la autenticacion es exitosa, notificamos al flujo principal de la app
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated){
            onAuthSuccess()
        }
    }
    //Si esta en RegisterScreen o LoginScreen
    if (isRegistering){
        RegisterScreen()

    } else {
        LoginScreen()

    }

}

