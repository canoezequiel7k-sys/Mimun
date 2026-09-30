package com.canoezequiel.moodflow.data.mapper

import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import java.time.LocalDateTime
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