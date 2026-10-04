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
import com.canoezequiel.moodflow.ui.theme.MimunSurfaceDark

//El contenedor raiz de navegacion. Contiene la NavigationBar inferior, persiste (Note, Mood, Analytics) y el navhost que intercambia las pantallas.
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = com.canoezequiel.moodflow.data.local.database.AppDatabase.getInstance(com.canoezequiel.moodflow.MoodApplication.context)
                val syncManager = com.canoezequiel.moodflow.data.remote.sync.SyncManager(
                    com.canoezequiel.moodflow.data.remote.api.ApiClient.apiService,
                    db.moodEntryDao(),
                    db.journalEntryDao(),
                    com.canoezequiel.moodflow.data.local.sync.SyncPreferences(com.canoezequiel.moodflow.MoodApplication.context)
                )
                syncManager.syncAll()
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MimunSurfaceDark
            ) {
                Screen.items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon,
                            contentDescription = screen.title) },
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
                AnalyticsScreen(onLogout = onLogout)
            }
        }
    }
}