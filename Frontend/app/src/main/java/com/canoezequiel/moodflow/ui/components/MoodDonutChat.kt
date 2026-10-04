package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.domain.model.MoodType

// Gráfico circular tipo dona (Donut Chart) para visualizar la proporción de estados de ánimo
@Composable
fun MoodDonutChart(
    moodPercentages: Map<MoodType, Float>,
    modifier: Modifier = Modifier
) {
    // [EXPLICACIÓN] Colores armónicos asociados a cada tipo de emoción
    fun getMoodColor(type: MoodType): Color {
        return when (type) {
            MoodType.RAD -> Color(0xFF08A127)   // Verde oliva
            MoodType.GOOD -> Color(0xFF2383CC)  // Verde suave
            MoodType.MEH -> Color(0xFF87C1DE)   // Gris crema
            MoodType.BAD -> Color(0xFFF85C29)   // Naranja suave
            MoodType.AWFUL -> Color(0xFFF44336)
        }
    }

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // [EXPLICACIÓN] Dibujamos los arcos proporcionales usando Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 26.dp.toPx()
            var startAngle = -90f // Iniciamos desde la parte superior del círculo

            moodPercentages.forEach { (moodType, percentage) ->
                if (percentage > 0f) {
                    // Calculamos los grados que ocupará este porcentaje sobre 360°
                    val sweepAngle = (percentage / 100f) * 360f
                    drawArc(
                        color = getMoodColor(moodType),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false, // false crea la forma de "dona" (anillo)
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        // Texto decorativo o informativo en el centro del círculo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "State",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "General",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}