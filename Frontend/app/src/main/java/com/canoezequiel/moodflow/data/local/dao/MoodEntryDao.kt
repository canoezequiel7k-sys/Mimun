package com.canoezequiel.moodflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity


//Objeto de acceso a datos (DAO) para realizar operaciones SQL sobre 'mood_entries'
@Dao
interface MoodEntryDao {

    // Obtiene solo las entradas activas (no eliminadas) para la UI
    @Query("SELECT * FROM mood_entries WHERE deletedAt IS NULL ORDER BY timestamp DESC")
    fun getAllMoodEntries(): List<MoodEntryEntity>

    // Consulta el registro guardado en un día específico (no eliminado)
    @Query("SELECT * FROM mood_entries WHERE substr(timestamp, 1, 10) = :dateString AND deletedAt IS NULL LIMIT 1")
    fun getMoodEntryByDate(dateString: String): MoodEntryEntity?

    @Query("SELECT * FROM mood_entries WHERE id = :id LIMIT 1")
    fun getMoodEntryById(id: String): MoodEntryEntity?

    // Inserta o reemplaza un registro
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMoodEntry(entry: MoodEntryEntity)

    // Borrado lógico (Tombstone): lo marca como pendiente de borrar para sincronizar
    @Query("UPDATE mood_entries SET syncStatus = 'PENDING_DELETE', deletedAt = :deletedAt WHERE substr(timestamp, 1, 10) = :dateString")
    fun softDeleteMoodEntriesByDate(dateString: String, deletedAt: String)

    @Query("UPDATE mood_entries SET syncStatus = 'PENDING_DELETE', deletedAt = :deletedAt WHERE id = :id")
    fun softDeleteMoodEntryById(id: String, deletedAt: String)

    // Borrado físico definitivo (se ejecuta solo después de confirmación 204/200 del servidor)
    @Query("DELETE FROM mood_entries WHERE id = :id")
    fun deleteMoodEntryPermanently(id: String)

    // Consultas para la sincronización (Tarea 4)
    @Query("SELECT * FROM mood_entries WHERE syncStatus != 'SYNCED'")
    fun getPendingMoodEntries(): List<MoodEntryEntity>

    @Query("UPDATE mood_entries SET syncStatus = 'SYNCED' WHERE id = :id")
    fun markAsSynced(id: String)

    @Query("DELETE FROM mood_entries")
    fun deleteAllMoodEntries()
}