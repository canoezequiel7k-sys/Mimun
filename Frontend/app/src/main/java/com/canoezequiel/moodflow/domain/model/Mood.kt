package com.canoezequiel.moodflow.domain.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//Informacion compatida de cada estado de animo.
data class Mood(
    val id: String,
    val name: String, //rad, good, meh, bad, awful
    val iconRes: Int, //Referencia al recurso drawable del icono
    val frames: List<Int> = emptyList(), // Lista de frames para animación frame-by-frame
    val colorHex: String,
    val iconSize: Dp = 56.dp
)
