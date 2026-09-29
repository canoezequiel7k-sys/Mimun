package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedMoodCharacter(
    frames: List<Int>, // Lista de drawables (frame1, frame2, frame3...)
    frameDurationMillis: Long = 100L // Velocidad de la animación
) {
    var currentFrameIndex by remember { mutableStateOf(0) }

    // Este efecto cambia de frame cíclicamente mientras el composable esté visible
    LaunchedEffect(frames) {
        while (true) {
            delay(frameDurationMillis)
            currentFrameIndex = (currentFrameIndex + 1) % frames.size
        }
    }

    // Mostramos el frame actual
    Image(
        painter = painterResource(id = frames[currentFrameIndex]),
        contentDescription = "Animated Mood",
        modifier = Modifier.size(140.dp)
    )
}