package com.canoezequiel.moodflow.domain.model

import java.time.LocalDate

//Modelo de datos procesado con las metricas y mapa de calendario para la pantalla de Analytics
data class AnalyticsSummary(
    val totalEntries: Int = 0,
    val moodCounts: Map<MoodType, Int> = emptyMap(),
    val moodPercentages: Map<MoodType, Float> = emptyMap(),
    val entriesByDate: Map<LocalDate, MoodEntry> = emptyMap()
)