package com.canoezequiel.moodflow.data.mapper

import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity
import com.canoezequiel.moodflow.data.local.remote.dto.JournalEntryDto
import com.canoezequiel.moodflow.data.local.remote.dto.JournalEntryUpsertRequest
import com.canoezequiel.moodflow.domain.model.JournalEntry
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun JournalEntryEntity.toDomain(): JournalEntry {
    return JournalEntry(
        id = id,
        title = title,
        content = content,
        timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        moodEntry = moodEntryId
    )
}

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

fun JournalEntry.toUpsertRequest(): JournalEntryUpsertRequest {
    val editedAtStr = timestamp.toInstant(ZoneOffset.UTC).toString()
    return JournalEntryUpsertRequest(
        moodEntryId = moodEntry,
        title = title,
        content = content,
        editedAt = editedAtStr
    )
}

fun JournalEntry.toDto(): JournalEntryDto {
    val editedAtStr = timestamp.toInstant(ZoneOffset.UTC).toString()
    return JournalEntryDto(
        id = id,
        title = title,
        content = content,
        moodEntryId = moodEntry,
        editedAt = editedAtStr
    )
}

fun JournalEntryDto.toDomain(): JournalEntry {
    val parsedTime = try {
        LocalDateTime.parse(createdAt?.substringBefore("Z"))
    } catch (e: Exception) {
        LocalDateTime.now()
    }
    return JournalEntry(
        id = id,
        title = title,
        content = content,
        timestamp = parsedTime ?: LocalDateTime.now(),
        moodEntry = moodEntryId
    )
}