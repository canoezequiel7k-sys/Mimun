package com.canoezequiel.moodflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.canoezequiel.moodflow.ui.screens.LoginScreen
import com.canoezequiel.moodflow.ui.screens.OnboardingScreen
import com.canoezequiel.moodflow.ui.screens.RegisterScreen
import com.canoezequiel.moodflow.ui.screens.SplashScreen
import kotlinx.coroutines.delay

/**
 * [RootNavGraph]
 * QUÉ HACE: Orquesta la navegación global de la app (Onboarding → Auth → Main App).
 * POR QUÉ: Separa los flujos iniciales y de login de la barra de navegación principal de la app.
 * CÓMO FUNCIONA: Controla las transiciones entre pantallas principales y limpia el historial (popUpTo).
 */

@Composable
fun RootNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash" //Ruta inicial al abri la app
    ){
        //Flujo 0: Splash
        composable("splash"){
            SplashScreen()

            //Esperamos 2 segundos (2000 ms) y saltamos al onboarding
            LaunchedEffect(Unit) {
                delay(1000)
                navController.navigate("onboarding"){
                    // Limpiamos el splash para que al presionar "Atras" no vuelva a aparecer
                    popUpTo("splash") { inclusive = true }
                }
            }
        }

        //Flujo 1: Onboarding
        composable("onboarding"){
            OnboardingScreen(
                onFinishOnboarding = {
                    //Al pulsar "Get Started", navegamos al login y borramos el onboarding del historial.
                    navController.navigate("login") {
                        popUpTo("onboarding") {inclusive = true}
                    }
                }
            )
        }

        //Flujo 2: Login
        composable("login"){
            LoginScreen(
//                //Aqui conectaras tu evento para ir a registro
//                onNavigateToRegister = {
//                    navController.navigate("register")
//                },
//                //Cuando el login es exitoso, entramos a la app principal
//                onLoginSuccess = {
//                    navController.navigate("main") {
//                        popUpTo("login") {inclusive = true}
//                    }
//                }
            )
        }

        //Flujo 3: Registro
        composable("register") {
            RegisterScreen(
//                //Volver al login si ya tiene cuenta
//                onBackToLogin = {
//                    navController.popBackStack()
//                },
//                //Si el registro es exitoso, tambien entramos a la app principal
//                onRegisterSuccess = {
//                    navController.navigate("main"){
//                        popUpTo("login") {inclusive = true}
//                    }
//                }
            )
        }

        //Flujo 4: App principal
        composable("main"){
            MainScreen()
        }
    }
}