package com.canoezequiel.moodflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.canoezequiel.moodflow.data.local.database.AppDatabase
import com.canoezequiel.moodflow.ui.navigation.RootNavGraph
import com.canoezequiel.moodflow.ui.theme.MoodFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Obligatorio para inicializar la API oficial del Splash Screen de Android
        //installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializamos la base de datos local SQLite de Room al arrancar la app
        AppDatabase.getInstance(this)

        setContent {
            MoodFlowTheme {
                RootNavGraph()
            }
        }
    }
}