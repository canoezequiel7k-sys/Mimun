package com.canoezequiel.moodflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


//tabla 'mood_entries' en la base de datos local SQlite para guardar registros de estado de animo
@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey val id: String,
    val moodType: String,
    val note: String?,
    val timestamp: String,
    val syncStatus: String = "PENDING", //Prevision para la sincronizacion con el servidor
    val updatedAt: String
)