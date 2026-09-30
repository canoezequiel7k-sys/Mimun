package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import java.time.LocalDate
import java.time.YearMonth

// Gráfico minimalista de onda/curva ("Gusanito Emocional")
@Composable
fun MoodWaveChart(
    currentMonth: YearMonth,
    entriesByDate: Map<LocalDate, MoodEntry>
) {
    val daysInMonth = currentMonth.lengthOfMonth()

    // Mapeo de niveles de emoción (RAD = 5, GOOD = 4, MEH = 3, BAD = 2, AWFUL = 1)
    fun getMoodLevel(type: MoodType): Float {
        return when (type) {
            MoodType.RAD -> 5f
            MoodType.GOOD -> 4f
            MoodType.MEH -> 3f
            MoodType.BAD -> 2f
            MoodType.AWFUL -> 1f
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Emotional Emotion mensually",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            val lineColor = MaterialTheme.colorScheme.primary

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val width = size.width
                val height = size.height
                val stepX = width / (daysInMonth - 1).coerceAtLeast(1)

                val points = mutableListOf<Pair<Float, Float>>()

                for (day in 1..daysInMonth) {
                    val date = currentMonth.atDay(day)
                    val entry = entriesByDate[date]
                    if (entry != null) {
                        val level = getMoodLevel(entry.moodType)
                        val x = (day - 1) * stepX
                        // Mapea nivel 1..5 a la altura Y (5 arriba, 1 abajo)
                        val y = height - ((level - 1f) / 4f * height)
                        points.add(Pair(x, y))
                    }
                }

                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points[0].first, points[0].second)
                        for (i in 1 until points.size) {
                            val p1 = points[i - 1]
                            val p2 = points[i]
                            val controlX1 = (p1.first + p2.first) / 2
                            cubicTo(controlX1, p1.second, controlX1, p2.second, p2.first, p2.second)
                        }
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }
        }
    }
}