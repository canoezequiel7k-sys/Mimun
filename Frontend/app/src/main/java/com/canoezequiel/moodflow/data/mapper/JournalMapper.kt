package com.canoezequiel.moodflow.data.mapper

import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity
import com.canoezequiel.moodflow.domain.model.JournalEntry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Convierte una entidad de notas de Room al modelo de Dominio
fun JournalEntryEntity.toDomain(): JournalEntry {
    return JournalEntry(
        id = id,
        title = title,
        content = content,
        timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        moodEntry = moodEntryId
    )
}

// Convierte un modelo de Dominio de notas a una entidad de Room
fun JournalEntry.toEntity(): JournalEntryEntity {
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val nowString = LocalDateTime.now().format(isoFormatter)
    return JournalEntryEntity(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp.format(isoFormatter),
        moodEntryId = moodEntry,
        syncStatus = "PENDING",
        updatedAt = nowString
    )
}