package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import com.canoezequiel.moodflow.ui.theme.MimunSurface
import java.time.LocalDate
import java.time.YearMonth

// Gráfico minimalista de onda/curva con líneas de referencia, eje Y a la derecha con guiones y emojis ordenados
@Composable
fun MoodWaveChart(
    currentMonth: YearMonth,
    entriesByDate: Map<LocalDate, MoodEntry>
) {
    val daysInMonth = currentMonth.lengthOfMonth()

    // Mapeo de niveles de emoción (RAD = 5 arriba, AWFUL = 1 abajo)
    fun getMoodLevel(type: MoodType): Float {
        return when (type) {
            MoodType.RAD -> 5f
            MoodType.GOOD -> 4f
            MoodType.MEH -> 3f
            MoodType.BAD -> 2f
            MoodType.AWFUL -> 1f
        }
    }

    // Estado para guardar las coordenadas (x, y) exactas del último punto de la onda
    var lastPointPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MimunSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your emotional evolution: ",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Fila principal: Gráfico a la izquierda y Eje Y (guiones y emojis) a la derecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. EL GRÁFICO DE ONDA CON LÍNEAS DE REFERENCIA (A la izquierda)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val lineColor = MaterialTheme.colorScheme.primary

                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val width = size.width
                        val height = size.height
                        val stepX = width / (daysInMonth - 1).coerceAtLeast(1)

                        val paddingTop = 16.dp.toPx()
                        val paddingBottom = 16.dp.toPx()
                        val usableHeight = height - paddingTop - paddingBottom

                        // Dibujar líneas de referencia horizontales (grid lines) para cada nivel 5..1
                        for (lvl in 1..5) {
                            val yLine = height - paddingBottom - ((lvl - 1f) / 4f * usableHeight)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                start = androidx.compose.ui.geometry.Offset(0f, yLine),
                                end = androidx.compose.ui.geometry.Offset(width, yLine),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        val points = mutableListOf<Pair<Float, Float>>()

                        for (day in 1..daysInMonth) {
                            val date = currentMonth.atDay(day)
                            val entry = entriesByDate[date]
                            if (entry != null) {
                                val level = getMoodLevel(entry.moodType)
                                val x = (day - 1) * stepX
                                val y = height - paddingBottom - ((level - 1f) / 4f * usableHeight)
                                points.add(Pair(x, y))
                            }
                        }

                        if (points.isNotEmpty()) {
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

                    // Icono flotante ic_graph posicionado exactamente en el centro de la última punta
                    lastPointPosition?.let { (x, y) ->
                        Image(
                            painter = painterResource(id = R.drawable.ic_graph),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .offset {
                                    IntOffset(
                                        x = (x - 16.dp.toPx()).toInt(),
                                        y = (y - 16.dp.toPx()).toInt()
                                    )
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 2. EJE Y CON GUIONES Y EMOJIS A LA DERECHA (De arriba: RAD -> AWFUL :Abajo)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("-", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(painter = painterResource(id = R.drawable.rad8), contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("-", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(painter = painterResource(id = R.drawable.good5), contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("-", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(painter = painterResource(id = R.drawable.meh1), contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("-", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(painter = painterResource(id = R.drawable.bad2), contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("-", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(painter = painterResource(id = R.drawable.awful3), contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
