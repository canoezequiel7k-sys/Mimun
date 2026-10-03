package com.canoezequiel.moodflow.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.canoezequiel.moodflow.MoodApplication
import com.canoezequiel.moodflow.data.local.auth.TokenManager
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
        startDestination = "splash", //Ruta inicial al abri la app
        // La pantalla nueva arranca a la derecha (initialOffsetX = { fullWidth }) y se desliza hacia el centro combinándose con fadeIn()
        enterTransition = {
            //{it} representa el ancho completo de la pantalla positivo (Borde derecho)
            slideInHorizontally(initialOffsetX = {it}, animationSpec = tween(1000)) + fadeIn(animationSpec = tween(1000))
        },
        exitTransition = {
            //{-it} representa el ancho completo negativo (Borde izquierdo)
            slideOutHorizontally(targetOffsetX = {-it}, animationSpec = tween(1000)) + fadeOut(animationSpec = tween(1000))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(1000)) + fadeIn(animationSpec = tween(1000))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(1000)) + fadeOut(animationSpec = tween(1000))
        }
    ){
        //Flujo 0: Splash
        composable("splash"){
            SplashScreen()

            val tokenManager = remember { TokenManager(MoodApplication.context) }

            //Esperamos 2 segundos (2000 ms) y saltamos al onboarding
            LaunchedEffect(Unit) {
                delay(1000)
                val hasToken = !tokenManager.getAccessToken().isNullOrBlank()
                val nextDestination = if (hasToken)  "main" else "onboarding"

                navController.navigate(nextDestination){
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
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("main"){
                        popUpTo("login") {inclusive = true}
                    }
                }
            )
        }

        //Flujo 3: Registro
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("register") {inclusive = true}
                    }
                }
            )
        }

        //Flujo 4: App principal
        composable("main"){
            MainScreen()
        }
    }
}