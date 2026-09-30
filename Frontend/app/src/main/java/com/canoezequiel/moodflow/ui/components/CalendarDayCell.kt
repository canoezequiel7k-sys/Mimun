package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.domain.model.Mood

// Celda individual de cada día en el calendario
@Composable
fun CalendarDayCell(
    dayNumber: Int,
    mood: Mood?
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(38.dp)
            .clip(CircleShape)
            .background(
                if (mood != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (mood != null) {
            // Muestra la imagen de la emoción si el día tiene registro
            Image(
                painter = painterResource(id = mood.iconRes),
                contentDescription = mood.name,
                modifier = Modifier.size(28.dp)
            )
        } else {
            // Muestra el número de día si no hay registro
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}