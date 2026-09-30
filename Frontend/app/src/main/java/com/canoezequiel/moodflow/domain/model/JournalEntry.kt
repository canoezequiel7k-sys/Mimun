package com.canoezequiel.moodflow.domain.model

import java.time.LocalDateTime
import java.util.UUID

//Representa una reflexion o nota del diario
data class JournalEntry(
    //Contiene un id único (UUID)
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val moodEntry: String? = null //ID opcional para relfexionar con la emocion del dia.
)