package com.canoezequiel.moodflow.data.mapper

import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity
import com.canoezequiel.moodflow.data.local.remote.dto.MoodEntryDto
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/**
 * [MoodMapperTest]
 * QUÉ HACE: Pruebas unitarias para verificar la conversión correcta entre las capas de Dominio, Room (Entidades) y Retrofit (DTOs).
 * POR QUÉ: Garantiza que los nombres de los campos (como 'mood' en lugar de 'moodType') y el formato de fechas no se corrompan al viajar entre capas.
 * CÓMO FUNCIONA:
 *   1. Crea un objeto simulado de origen.
 *   2. Aplica la función de mapeo (ej. toDomain() o toUpsertRequest()).
 *   3. Comprueba (assertEquals) que los valores resultantes coincidan exactamente con el contrato esperado.
 */
class MoodMapperTest {

    @Test
    fun moodEntryEntity_toDomain_correctlyMapsFields() {
        // QUÉ HACE: Verifica que una entidad guardada en SQLite (Room) se convierta correctamente a un modelo de Dominio.
        val entity = MoodEntryEntity(
            id = "uuid-123",
            moodType = "RAD",
            note = "Día excelente",
            timestamp = "2026-10-03T10:00:00",
            syncStatus = "PENDING",
            updatedAt = "2026-10-03T10:00:00Z"
        )

        val domain = entity.toDomain()

        assertEquals("uuid-123", domain.id)
        assertEquals(MoodType.RAD, domain.moodType)
        assertEquals("Día excelente", domain.note)
    }

    @Test
    fun moodEntryDto_toDomain_correctlyMapsMoodField() {
        // QUÉ HACE: Verifica que la propiedad 'mood' del JSON de la API se mapee al enum 'MoodType' de Dominio.
        val dto = MoodEntryDto(
            id = "uuid-456",
            mood = "GOOD",
            note = "Buena jornada",
            date = "2026-10-03"
        )

        val domain = dto.toDomain()

        assertEquals("uuid-456", domain.id)
        assertEquals(MoodType.GOOD, domain.moodType)
        assertEquals("Buena jornada", domain.note)
    }

    @Test
    fun moodEntry_toUpsertRequest_correctlyFormatsDateAndMood() {
        // QUÉ HACE: Verifica que un modelo de Dominio genere el payload estricto requerido por el PUT de FastAPI.
        val entry = MoodEntry(
            id = "uuid-789",
            moodType = MoodType.AWFUL,
            note = "Día difícil",
            timestamp = LocalDateTime.of(2026, 10, 3, 15, 30)
        )

        val request = entry.toUpsertRequest()

        assertEquals("AWFUL", request.mood)
        assertEquals("2026-10-03", request.date)
        assertEquals("Día difícil", request.note)
    }
}