package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.ui.navigation.Screen.Companion.items
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


//Componente visual de calendario mensual con iconos emocionales
@Composable
fun MoodCalendarView(
    currentMonth: YearMonth, // <- Cambiar 'currentsMoth' por 'currentMonth'
    entriesByDate: Map<LocalDate, MoodEntry>,
    availableMoods: List<Mood>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
    val daysOfWeek = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
            }

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            userScrollEnabled = false
        ) {
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(36.dp))
            }

            items(daysInMonth) { dayIndex ->
                val dayNumber = dayIndex + 1
                val date = currentMonth.atDay(dayNumber)
                val entry = entriesByDate[date]
                val mood = availableMoods.find { it.id == entry?.moodType?.name }

                CalendarDayCell(
                    dayNumber = dayNumber,
                    mood = mood
                )
            }
        }
    }
}