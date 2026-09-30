package com.canoezequiel.moodflow.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.domain.model.Mood

//Carrusel horizontal (LazyRow) con las 6 emociones y animaciones elasticas de escala y rotacion al seleccionar.
@Composable
fun MoodHeaderRow(
    moods: List<Mood>,
    selectedMood: Mood?,
    onMoodSelected: (Mood) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(moods) { mood ->
            val isSelected = mood == selectedMood

            // Animación de escala elástica
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )

            // Animación de rotación oscilante en zig-zag
            val rotation by animateFloatAsState(
                targetValue = if (isSelected) 6f else 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
            )

            Column(
                modifier = Modifier
                    .scale(scale)
                    .rotate(rotation)
                    .clickable { onMoodSelected(mood) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emoji animado (reproduce sus frames al ser seleccionado)
                AnimatedMoodIcon(
                    frames = mood.frames,
                    defaultIcon = mood.iconRes,
                    isSelected = isSelected,
                    size = mood.iconSize
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Texto debajo del emoji
                Text(
                    text = mood.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}