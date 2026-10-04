package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R // Asegúrate de importar tus recursos
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import com.canoezequiel.moodflow.ui.theme.MimunSurface
import java.time.LocalDate
import java.time.YearMonth

// Gráfico minimalista de onda/curva ("Gusanito Emocional") con icono decorativo en la punta
@Composable
fun MoodWaveChart(
    currentMonth: YearMonth,
    entriesByDate: Map<LocalDate, MoodEntry>
) {
    val daysInMonth = currentMonth.lengthOfMonth()

    //[EXPLICACIÓN] Mapeo de niveles de emoción para calcular la altura 'Y' de la onda
    fun getMoodLevel(type: MoodType): Float {
        return when (type) {
            MoodType.RAD -> 5f
            MoodType.GOOD -> 4f
            MoodType.MEH -> 3f
            MoodType.BAD -> 2f
            MoodType.AWFUL -> 1f
        }
    }

    // [EXPLICACIÓN] Estado para guardar las coordenadas (x, y) exactas del último punto de la onda
    var lastPointPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MimunSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your emotional evolution",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(28.dp))

            val lineColor = MaterialTheme.colorScheme.primary

            //[EXPLICACIÓN] Usamos un Box para superponer el Canvas y la imagen de la punta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                //DIBUJO DE LA ONDA CON CANVAS
                Canvas(
                    modifier = Modifier.fillMaxSize()
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
                            val paddingTop = 24.dp.toPx()
                            val paddingBottom = 24.dp.toPx()
                            val usableHeight = height - paddingTop - paddingBottom
                            val y = height - paddingBottom - ((level - 1f) / 4f * usableHeight)
                            points.add(Pair(x, y))
                        }
                    }

                    if (points.isNotEmpty()) {
                        // [EXPLICACIÓN] Guardamos el último punto visual de la curva
                        lastPointPosition = points.last()
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

                //ICONO FLOTANTE EN LA PUNTA (ic_graph)
                // [POR QUÉ] Simula el detalle orgánico de la referencia visual
                lastPointPosition?.let { (x, y) ->
                    Image(
                        painter = painterResource(id = R.drawable.ic_graph),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .offset {
                                // Centramos el icono de 32dp restando la mitad en X y un poco más arriba en Y
                                IntOffset(
                                    x = (x - 16.dp.toPx()).toInt(),
                                    y = (y - 16.dp.toPx()).toInt() // Restamos 16.dp (la mitad de 32dp) también en Y para centrarlo
                                )
                            }
                    )
                }
            }
        }
    }
}