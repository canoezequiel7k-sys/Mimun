package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.domain.model.AnalyticsSummary
import com.canoezequiel.moodflow.domain.repository.MoodRepository

//Caso de uso que procesa todas las entradas guardadas en Room para calcular los conteos por emoción, los porcentajes sobre el total y organizar las entradas en un mapa por fecha (LocalDate).
class GetAnalyticsUseCase(
    private val repository: MoodRepository
) {
    operator fun invoke(): AnalyticsSummary{
        val entries = repository.getAllMoodEntries()
        val total = entries.size

        if (total == 0) return AnalyticsSummary()

        //Agrupar conteo por tipo de emocion
        val counts = entries.groupingBy { it.moodType }.eachCount()

        //Calcular porcentaje por emoción
        val percentages = counts.mapValues { (_, count) ->
            (count.toFloat() / total) * 100f
        }

        // Mapear por fecha tomando la última emoción guardada de cada día
        val entriesByDate = entries
            .sortedBy { it.timestamp }
            .associateBy { it.timestamp.toLocalDate() }

        return AnalyticsSummary(
            totalEntries = total,
            moodCounts = counts,
            moodPercentages = percentages,
            entriesByDate = entriesByDate
        )
    }
}