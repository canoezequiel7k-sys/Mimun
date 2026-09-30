package com.canoezequiel.moodflow.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.canoezequiel.moodflow.ui.screens.AnalyticsScreen
import com.canoezequiel.moodflow.ui.screens.MoodSelectionScreen
import com.canoezequiel.moodflow.ui.screens.NotesScreen

//El contenedor raiz de navegacion. Contiene la NavigationBar inferior, persiste (Note, Mood, Analytics) y el navhost que intercambia las pantallas.
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Mood.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Notes.route) {
                NotesScreen()
            }
            composable(Screen.Mood.route) {
                MoodSelectionScreen()
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
        }
    }
}