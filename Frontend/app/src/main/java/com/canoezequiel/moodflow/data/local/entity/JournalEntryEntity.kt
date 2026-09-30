package com.canoezequiel.moodflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**define la tabla journal_entries en SQLite para almacenar las notas y reflexiones del diario personal*/
//tabla 'journal_entries' en la base de datos SQLite para guardar reflexiones
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val timestamp: String,
    val moodEntryId: String?, //ID opcional vinculado a la emocion del día
    val syncStatus: String = "PENDING",
    val updatedAt: String
)
