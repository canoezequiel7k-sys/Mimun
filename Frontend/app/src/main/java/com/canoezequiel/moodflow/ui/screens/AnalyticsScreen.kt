package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.domain.model.MoodType
import com.canoezequiel.moodflow.ui.components.MimunTab
import com.canoezequiel.moodflow.ui.components.MoodCalendarView
import com.canoezequiel.moodflow.ui.components.MoodDistributionItem
import com.canoezequiel.moodflow.ui.components.MoodDonutChart
import com.canoezequiel.moodflow.ui.components.MoodWaveChart
import com.canoezequiel.moodflow.ui.theme.MimunBackground
import com.canoezequiel.moodflow.ui.theme.MimunGreen
import com.canoezequiel.moodflow.ui.viewmodel.AnalyticsViewModel

//Pantalla de grafico y calendario emocional
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estado para controlar la pestaña activa (0 = Calendario, 1 = Evolución, 2 = Distribución)
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.graph_title),
                        contentDescription = null,
                        modifier = Modifier
                            .height(28.dp)
                            .width(28.dp)
                            .padding(horizontal = 4.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MimunBackground,
                    navigationIconContentColor = MimunGreen
                )
            )
        }
    ) { innerPadding ->
        // Contenedor principal con un único scroll vertical
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()) // <--- Único scroll de la pantalla
        ) {

            // Pestañas superiores (Tabs)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                indicator = {},
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                MimunTab(
                    text = "Calendar",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )

                MimunTab(
                    text = "Evolution",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )

                MimunTab(
                    text = "Distribution",
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Contenido dinámico según la pestaña seleccionada (SIN segundo scroll)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> {
                        MoodCalendarView(
                            currentMonth = uiState.currentMonth,
                            entriesByDate = uiState.summary.entriesByDate,
                            availableMoods = uiState.availableMoods,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() }
                        )
                    }
                    1 -> {
                        if (uiState.summary.totalEntries > 0) {
                            MoodWaveChart(
                                currentMonth = uiState.currentMonth,
                                entriesByDate = uiState.summary.entriesByDate
                            )
                        } else {
                            Text(
                                text = "There is not yet enough recorded data regarding the evolution.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.summary.totalEntries == 0) {
                                Text(
                                    text = "There is not yet enough data to calculate the distribution.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                // Mostramos el gráfico circular centrado
                                MoodDonutChart(moodPercentages = uiState.summary.moodPercentages)

                                Spacer(modifier = Modifier.height(8.dp))

                                // Luego listamos las barras de porcentaje individuales que ya tenías
                                uiState.availableMoods.forEach { mood ->
                                    val moodType = try { MoodType.valueOf(mood.id) } catch (e: Exception) { null }
                                    val count = uiState.summary.moodCounts[moodType] ?: 0
                                    val percentage = uiState.summary.moodPercentages[moodType] ?: 0f

                                    MoodDistributionItem(
                                        mood = mood,
                                        count = count,
                                        percentage = percentage
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}