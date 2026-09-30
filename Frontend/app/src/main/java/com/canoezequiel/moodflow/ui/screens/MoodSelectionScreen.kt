package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.ui.components.MoodHeaderRow
import com.canoezequiel.moodflow.ui.viewmodel.MoodViewModel

//Pantalla principal de seleccion emocional, muestra el carrusel MoodHeaderRow, el campo de nota opcional y el boton flotante para guarda.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodSelectionScreen(
    viewModel: MoodViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How are you?") }
            )
        },
        floatingActionButton = {
            if (uiState.selectedMood != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.saveTodayMood() },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Save") },
                    text = { Text("Save State") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Selector horizontal superior con columnas (Emoji + Texto) y animación de frames
            MoodHeaderRow(
                moods = uiState.moods,
                selectedMood = uiState.selectedMood,
                onMoodSelected = { viewModel.selectMood(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de entrada para nota corta opcional
            OutlinedTextField(
                value = uiState.noteText,
                onValueChange = { viewModel.updateNoteText(it) },
                label = { Text("Nota opcional sobre tu día...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 4
            )
            //Contenido central con imagen reservada y texto guía
            if (uiState.showSuccessMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¡Registro diario guardado con éxito! ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}