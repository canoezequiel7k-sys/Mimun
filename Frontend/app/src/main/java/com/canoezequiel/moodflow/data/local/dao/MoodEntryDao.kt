package com.canoezequiel.moodflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity


//Objeto de acceso a datos (DAO) para realizar operaciones SQL sobre 'mood_entries'
@Dao
interface MoodEntryDao {

    //Obtiene todas las entradas ordenadas desde la mas reciente
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoodEntries(): List<MoodEntryEntity>

    //Consulta el registro guardado en un dia especifico (compara los primeros 10 caracteres YYYY-MM-DD)
    @Query("SELECT * FROM mood_entries WHERE substr(timestamp, 1, 10) = :dateString LIMIT 1")
    fun getMoodEntryByDate(dateString: String): MoodEntryEntity?

    //Inserta una entrada o la reemplaza si ya existe en esa misma fecha
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMoodEntry(entry: MoodEntryEntity)

    //Elimina cualquier registro existente para la misma fecha (YYYY-MM-DD)
    @Query("DELETE FROM mood_entries WHERE substr(timestamp, 1, 10) = :dateString")
    fun deleteMoodEntriesByDate(dateString: String)
}