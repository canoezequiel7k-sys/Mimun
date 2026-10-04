package com.canoezequiel.moodflow.data.mapper

import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity
import com.canoezequiel.moodflow.data.local.remote.dto.JournalEntryDto
import com.canoezequiel.moodflow.data.local.remote.dto.JournalEntryUpsertRequest
import com.canoezequiel.moodflow.domain.model.JournalEntry
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//Convierte de entidad local (Room) a modelo de dominio
fun JournalEntryEntity.toDomain(): JournalEntry {
    return JournalEntry(
        id = id,
        title = title,
        content = content,
        timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        moodEntry = moodEntryId,
        icon = icon, //Mapea el icono
        emoji = emoji //Mapea el emoji
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
        icon = icon,    // Guarda el icono
        emoji = emoji,  //Guarda el emoji
        syncStatus = "PENDING",
        updatedAt = nowString
    )
}

// Prepara el objeto para enviar al Servidor (Upsert)
fun JournalEntry.toUpsertRequest(): JournalEntryUpsertRequest {
    val editedAtStr = timestamp.atZone(ZoneId.systemDefault()).toInstant().toString()
    return JournalEntryUpsertRequest(
        moodEntryId = moodEntry,
        title = title,
        content = content,
        editedAt = editedAtStr
        // Nota: Si tu backend también aceptará icono y emoji,
        // aquí los incluirás en el Request cuando tu otro agente los configure en el servidor.
    )
}

fun JournalEntry.toDto(): JournalEntryDto {
    val editedAtStr = timestamp.atZone(ZoneId.systemDefault()).toInstant().toString()
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