package com.canoezequiel.moodflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity

// Objeto de acceso a datos (DAO) para realizar operaciones SQL sobre 'journal_entries'
@Dao
interface JournalEntryDao {

    // Consulta todas las reflexiones ordenadas por fecha reciente
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): List<JournalEntryEntity>

    // Obtiene una nota por su ID único
    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    fun getJournalEntryById(id: String): JournalEntryEntity?

    // Inserta una nueva nota o reemplaza la existente en caso de edición
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJournalEntry(entry: JournalEntryEntity)

    // Elimina una nota de la base de datos por su ID
    @Query("DELETE FROM journal_entries WHERE id = :id")
    fun deleteJournalEntryById(id: String)
}