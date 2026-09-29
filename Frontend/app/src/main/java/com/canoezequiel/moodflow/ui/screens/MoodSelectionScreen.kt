package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.ui.components.CenterEmptyStateContent
import com.canoezequiel.moodflow.ui.components.MoodBottomBar
import com.canoezequiel.moodflow.ui.components.MoodHeaderRow
import com.canoezequiel.moodflow.ui.viewmodel.MoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodSelectionScreen(
    viewModel: MoodViewModel = viewModel()
) {
    val selectedMood by viewModel.selectedMood.collectAsState()
    val moods = viewModel.moods

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How are you?") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //Selector horizontal superior con columnas (Emoji + Texto) y animación de frames
            MoodHeaderRow(
                moods = moods,
                selectedMood = selectedMood,
                onMoodSelected = { viewModel.selectMood(it) }
            )

            //Contenido central con imagen reservada y texto guía
            CenterEmptyStateContent(
                modifier = Modifier.weight(1f)
            )
        }
    }
}