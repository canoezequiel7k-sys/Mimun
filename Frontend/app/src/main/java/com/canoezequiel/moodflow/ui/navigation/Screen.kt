package com.canoezequiel.moodflow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mood
import androidx.compose.ui.graphics.vector.ImageVector

//define las 3 rutas principales navegables (Notes, Mood, Analytics) con sus nombres de ruta e íconos de Material 3.
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
){
    object Notes: Screen("notes", "Notes", Icons.Default.EditNote)
    object Mood: Screen("mood", "Mood", Icons.Default.Mood)
    object Analytics: Screen("analytics", "Analytics", Icons.Default.Analytics)

    companion object{
        val items = listOf(Notes, Mood, Analytics)
    }
}