package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.model.MoodType
import com.canoezequiel.moodflow.ui.components.MoodCalendarView
import com.canoezequiel.moodflow.ui.components.MoodDistributionItem
import com.canoezequiel.moodflow.ui.components.MoodWaveChart
import com.canoezequiel.moodflow.ui.viewmodel.AnalyticsViewModel

//Pantalla de grafico y calendario emocional
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recarga las estadísticas cada vez que se navega a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Estadísticas e Historial") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Tarjeta de resumen de total de días
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total de días registrados: ",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${uiState.summary.totalEntries}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Calendario Mensual Emocional
            Text(
                text = "Calendario Emocional",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            MoodCalendarView(
                currentMonth = uiState.currentMonth,
                entriesByDate = uiState.summary.entriesByDate,
                availableMoods = uiState.availableMoods,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gráfico minimalista de curva de evolución emocional ("Gusanito")
            if (uiState.summary.totalEntries > 0) {
                MoodWaveChart(
                    currentMonth = uiState.currentMonth,
                    entriesByDate = uiState.summary.entriesByDate
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            //Desglose de distribución de emociones
            Text(
                text = "Distribución de Emociones",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.summary.totalEntries == 0) {
                Text(
                    text = "Aún no hay suficientes datos registrados para generar estadísticas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.availableMoods.forEach { mood ->
                    val moodType = try { MoodType.valueOf(mood.id) } catch (e: Exception) { null }
                    val count = uiState.summary.moodCounts[moodType] ?: 0
                    val percentage = uiState.summary.moodPercentages[moodType] ?: 0f

                    MoodDistributionItem(
                        mood = mood,
                        count = count,
                        percentage = percentage
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}