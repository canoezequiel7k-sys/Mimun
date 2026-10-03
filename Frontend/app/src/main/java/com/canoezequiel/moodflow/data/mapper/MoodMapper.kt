package com.canoezequiel.moodflow.data.mapper

import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity
import com.canoezequiel.moodflow.data.local.remote.dto.MoodEntryDto
import com.canoezequiel.moodflow.data.local.remote.dto.MoodEntryUpsertRequest
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


//Funciones de extensión para convertir entre MoodEntryEntity de Room y MoodEntry de Dominio.

//Convierte una entidad de la base de datos Room a un objeto del Dominio
fun MoodEntryEntity.toDomain(): MoodEntry{
    return MoodEntry(
        id = id,
        moodType = try {
            MoodType.valueOf(moodType)
        } catch (e: Exception){
            MoodType.GOOD
        },
        note = note,
        timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}

//Convierte un objeto del dominio a una entidad lista para guardarse en room
fun MoodEntry.toEntity(): MoodEntryEntity {
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val nowString = LocalDateTime.now().format(isoFormatter)
    return MoodEntryEntity(
        id = id,
        moodType = moodType.name,
        note = note,
        timestamp = timestamp.format(isoFormatter),
        syncStatus = "PENDING",
        updatedAt = nowString
    )
}

// Transforma Dominio → DTO (para enviar al servidor)
fun MoodEntry.toUpsertRequest(): MoodEntryUpsertRequest {
    val dateStr = timestamp.toLocalDate().toString()
    val editedAtStr = timestamp.atZone(ZoneId.systemDefault()).toInstant().toString()
    return MoodEntryUpsertRequest(
        mood = moodType.name,
        date = dateStr,
        note = note,
        editedAt = editedAtStr
    )
}

// Transforma DTO → Dominio (para usar en la app)
fun MoodEntryDto.toDomain(): MoodEntry {
    val parsedDate = LocalDate.parse(date).atStartOfDay()
    return MoodEntry(
        id = id,
        moodType = try { MoodType.valueOf(mood) } catch (e: Exception) { MoodType.GOOD },
        note = note,
        timestamp = parsedDate
    )
}