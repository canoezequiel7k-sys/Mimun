package com.canoezequiel.moodflow.domain.model

import java.time.LocalDateTime
import java.util.UUID

//representa una entrada registrada por el usuario. Tiene id UUID), moodType, note(Nota corta opcional) y timestamp(Fecha)
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val moodType: MoodType,
    val note: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)