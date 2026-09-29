package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedMoodIcon(
    frames: List<Int>,
    defaultIcon: Int,
    isSelected: Boolean,
    frameDurationMillis: Long = 100L,
    size: Dp
) {
    var currentFrameIndex by remember { mutableStateOf(0) }

    // Cuando se selecciona, reproduce la secuencia de frames una sola vez
    LaunchedEffect(isSelected) {
        if (isSelected && frames.isNotEmpty()) {
            for (i in frames.indices) {
                currentFrameIndex = i
                delay(frameDurationMillis)
            }
        } else {
            currentFrameIndex = 0
        }
    }

    val imageRes = if (isSelected && frames.isNotEmpty()) frames[currentFrameIndex] else defaultIcon

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        modifier = Modifier.size(56.dp)
    )
}

