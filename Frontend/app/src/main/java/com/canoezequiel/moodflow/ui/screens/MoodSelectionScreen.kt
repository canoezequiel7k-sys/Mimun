package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.ui.components.MoodHeaderRow
import com.canoezequiel.moodflow.ui.theme.MimunBackground
import com.canoezequiel.moodflow.ui.theme.MimunGreen
import com.canoezequiel.moodflow.ui.theme.MimunPrimaryContainer
import com.canoezequiel.moodflow.ui.theme.MimunSurface
import com.canoezequiel.moodflow.ui.theme.MimunTextPrimary
import com.canoezequiel.moodflow.ui.theme.MimunTextSecondary
import com.canoezequiel.moodflow.ui.theme.TESTCOLOR
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
                title = {
                    Text(
                        text = "Mimun",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_great),
                        contentDescription = null,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MimunBackground,
                    titleContentColor = MimunTextPrimary,
                    navigationIconContentColor = MimunGreen
                )
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MimunBackground)
        ) {
            Image(
                painter = painterResource(
                    R.drawable.test_background
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth() // Ocupa todo el ancho horizontal de la pantalla
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.FillWidth, // Escala la imagen para que coincida exactamente con el ancho
                alpha = 0.8f
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                //Textos
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "How do you feel today?",
                        fontSize = 20.sp,
                        color = MimunTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your emotional state matters",
                        fontSize = 16.sp,
                        color = MimunTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                    onValueChange = {
                        //Si el usuario intenta escribir 201, simplemente se ignora y no se actualiza
                        if (it.length <= 200) {
                            viewModel.updateNoteText(it)
                        }
                    },
                    label = { Text("Add note") },
                    placeholder = {
                        Text(
                            text = "✎ Do you want to add a note for the day?",
                            color = MimunTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MimunSurface,
                        unfocusedContainerColor = MimunSurface
                    ),
                    supportingText = {
                        Box(modifier = Modifier.fillMaxWidth()){
                            Text(
                                text = "${uiState.noteText.length}/200",
                                modifier = Modifier.align(Alignment.CenterEnd),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp)
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
}