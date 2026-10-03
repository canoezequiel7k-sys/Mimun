package com.canoezequiel.moodflow.data.repository

import com.canoezequiel.moodflow.data.local.dao.MoodEntryDao
import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/**
 * [MoodRepositoryTest]
 * QUÉ HACE: Valida la regla de negocio de reutilización del ID del día para evitar conflictos 409 en el backend.
 * POR QUÉ: Si el usuario edita el estado de un día existente, debemos preservar el ID original de esa fecha.
 * CÓMO FUNCIONA: Simula una base de datos local en memoria (Fake DAO) y comprueba que al guardar un segundo estado para la misma fecha, el ID resultante sea el original.
 */
class MoodRepositoryTest {

    /**
     * Fake MoodEntryDao en memoria para aislar las pruebas unitarias sin necesitar la base de datos de Android.
     */
    private class FakeMoodEntryDao : MoodEntryDao {
        val storage = mutableMapOf<String, MoodEntryEntity>()

        override fun getAllMoodEntries(): List<MoodEntryEntity> = storage.values.toList()

        override fun getMoodEntryByDate(dateString: String): MoodEntryEntity? {
            return storage.values.find { it.timestamp.startsWith(dateString) && it.deletedAt == null }
        }

        override fun getMoodEntryById(id: String): MoodEntryEntity? = storage[id]

        override fun insertMoodEntry(entry: MoodEntryEntity) {
            storage[entry.id] = entry
        }

        override fun softDeleteMoodEntriesByDate(dateString: String, deletedAt: String) {}
        override fun softDeleteMoodEntryById(id: String, deletedAt: String) {}
        override fun deleteMoodEntryPermanently(id: String) { storage.remove(id) }
        override fun getPendingMoodEntries(): List<MoodEntryEntity> = storage.values.filter { it.syncStatus != "SYNCED" }
        override fun markAsSynced(id: String) {}
    }

    @Test
    fun saveMoodEntry_whenDateAlreadyExists_reusesExistingId() {
        // QUÉ HACE: Prueba que al registrar un nuevo ánimo para hoy, se conserve el ID si ya existía un registro previo.
        val fakeDao = FakeMoodEntryDao()
        val repository = MoodRepositoryImpl(fakeDao)

        // 1. Guardamos el primer estado del día con el ID original "id-original-100"
        val firstEntry = MoodEntry(
            id = "id-original-100",
            moodType = MoodType.MEH,
            note = "Mañana regular",
            timestamp = LocalDateTime.of(2026, 10, 3, 9, 0)
        )
        repository.saveMoodEntry(firstEntry)

        // 2. Intentamos guardar un segundo estado para el mismo día asignando un ID nuevo "id-nuevo-999"
        val secondEntry = MoodEntry(
            id = "id-nuevo-999",
            moodType = MoodType.RAD,
            note = "Tarde fantástica",
            timestamp = LocalDateTime.of(2026, 10, 3, 18, 0)
        )
        repository.saveMoodEntry(secondEntry)

        // 3. Comprobamos que el ID guardado sea el original "id-original-100"
        val savedInDb = fakeDao.getMoodEntryByDate("2026-10-03")
        assertEquals("id-original-100", savedInDb?.id)
        assertEquals("RAD", savedInDb?.moodType)
    }
}