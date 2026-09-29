package com.canoezequiel.moodflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.canoezequiel.moodflow.ui.navigation.MainScreen
import com.canoezequiel.moodflow.ui.theme.MoodFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoodFlowTheme {
                MainScreen()
            }
        }
    }
}