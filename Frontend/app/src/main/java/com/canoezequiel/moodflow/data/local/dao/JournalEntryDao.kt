package com.canoezequiel.moodflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity

// Objeto de acceso a datos (DAO) para realizar operaciones SQL sobre 'journal_entries'
@Dao
interface JournalEntryDao {

    // Obtiene solo reflexiones activas (no eliminadas) para la UI
    @Query("SELECT * FROM journal_entries WHERE deletedAt IS NULL ORDER BY timestamp DESC")
    fun getAllJournalEntries(): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    fun getJournalEntryById(id: String): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJournalEntry(entry: JournalEntryEntity)

    // Borrado lógico (Tombstone): lo marca como pendiente de borrar para el servidor
    @Query("UPDATE journal_entries SET syncStatus = 'PENDING_DELETE', deletedAt = :deletedAt WHERE id = :id")
    fun softDeleteJournalEntryById(id: String, deletedAt: String)

    // Borrado físico definitivo (se ejecuta tras confirmar con el servidor)
    @Query("DELETE FROM journal_entries WHERE id = :id")
    fun deleteJournalEntryPermanently(id: String)

    // Consultas para la sincronización (Tarea 4)
    @Query("SELECT * FROM journal_entries WHERE syncStatus != 'SYNCED'")
    fun getPendingJournalEntries(): List<JournalEntryEntity>

    @Query("UPDATE journal_entries SET syncStatus = 'SYNCED' WHERE id = :id")
    fun markAsSynced(id: String)

    @Query("DELETE FROM journal_entries")
    fun deleteAllJournalEntries()
}