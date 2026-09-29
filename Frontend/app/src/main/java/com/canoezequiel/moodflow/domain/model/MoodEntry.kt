package com.canoezequiel.moodflow.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val moodType: MoodType,
    val note: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)