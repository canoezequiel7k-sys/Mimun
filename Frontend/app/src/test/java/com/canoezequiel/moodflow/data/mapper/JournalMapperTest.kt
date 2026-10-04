package com.canoezequiel.moodflow.data.mapper

import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity
import com.canoezequiel.moodflow.data.local.remote.dto.JournalEntryDto
import com.canoezequiel.moodflow.domain.model.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

/**
 * [JournalMapperTest]
 * QUÉ HACE: Pruebas unitarias exhaustivas para verificar el mapeo de notas y reflexiones ([JournalEntry])
 *           entre la base de datos local (Room), el Dominio y los objetos de transferencia de red (DTOs/Requests).
 * POR QUÉ: Asegura que los nuevos campos añadidos recientemente ('icon' y 'moodEntryId') viajen sin pérdidas
 *           de información a través de toda la arquitectura de la aplicación.
 * CASUÍSTICAS PROBADAS:
 *   1. Mapeo de Entidad Room a Dominio (con y sin icono/emoción).
 *   2. Mapeo de Dominio a Entidad Room.
 *   3. Mapeo de Dominio a Objeto Upsert para la API (Retrofit).
 *   4. Mapeo de DTO (Respuesta de Red) a Dominio.
 */
class JournalMapperTest {

    @Test
    fun journalEntryEntity_toDomain_withIconAndMood_correctlyMapsFields() {
        // CASUÍSTICA: Una nota guardada en SQLite que incluye icono aleatorio y vinculación de estado de ánimo.
        val entity = JournalEntryEntity(
            id = "note-uuid-1",
            title = "Reflexión matutina",
            content = "Hoy me sentí muy en paz con todo.",
            timestamp = "2026-10-04T08:00:00",
            moodEntryId = "mood-uuid-999",
            icon = "sun",
            syncStatus = "SYNCED",
            updatedAt = "2026-10-04T08:00:00Z"
        )

        val domain = entity.toDomain()

        assertEquals("note-uuid-1", domain.id)
        assertEquals("Reflexión matutina", domain.title)
        assertEquals("Hoy me sentí muy en paz con todo.", domain.content)
        assertEquals("mood-uuid-999", domain.moodEntry)
        assertEquals("sun", domain.icon)
    }

    @Test
    fun journalEntryEntity_toDomain_nullIconAndMood_handlesSafely() {
        // CASUÍSTICA: Notas antiguas o creadas sin icono ni emoción vinculada (valores nulos).
        val entity = JournalEntryEntity(
            id = "note-uuid-2",
            title = "Nota simple",
            content = "Sin adornos.",
            timestamp = "2026-10-04T09:00:00",
            moodEntryId = null,
            icon = null,
            syncStatus = "PENDING",
            updatedAt = "2026-10-04T09:00:00Z"
        )

        val domain = entity.toDomain()

        assertEquals("note-uuid-2", domain.id)
        assertNull(domain.moodEntry)
        assertNull(domain.icon)
    }

    @Test
    fun journalEntry_toEntity_correctlyPreservesCustomFields() {
        // CASUÍSTICA: Conversión del modelo de dominio hacia la entidad de Room antes de persistir.
        val domain = JournalEntry(
            id = "note-uuid-3",
            title = "Atardecer",
            content = "Hermoso cielo hoy.",
            timestamp = LocalDateTime.of(2026, 10, 4, 19, 0),
            moodEntry = "mood-uuid-888",
            icon = "apple"
        )

        val entity = domain.toEntity()

        assertEquals("note-uuid-3", entity.id)
        assertEquals("Atardecer", entity.title)
        assertEquals("Hermoso cielo hoy.", entity.content)
        assertEquals("mood-uuid-888", entity.moodEntryId)
        assertEquals("apple", entity.icon)
        assertEquals("PENDING", entity.syncStatus)
    }

    @Test
    fun journalEntry_toUpsertRequest_includesIconAndMoodReference() {
        // CASUÍSTICA: Generación del payload JSON para sincronizar con el backend vía PUT.
        val domain = JournalEntry(
            id = "note-uuid-4",
            title = "Ideas de código",
            content = "Clean Architecture es genial.",
            timestamp = LocalDateTime.of(2026, 10, 4, 10, 0),
            moodEntry = "mood-uuid-777",
            icon = "sky"
        )

        val request = domain.toUpsertRequest()

        assertEquals("Ideas de código", request.title)
        assertEquals("Clean Architecture es genial.", request.content)
        assertEquals("mood-uuid-777", request.moodEntryId)
        assertEquals("sky", request.icon)
    }

    @Test
    fun journalEntryDto_toDomain_correctlyParsesServerResponse() {
        // CASUÍSTICA: Procesamiento de la respuesta de la API (DTO) al descargar notas del servidor.
        val dto = JournalEntryDto(
            id = "note-uuid-5",
            title = "Sincronizado",
            content = "Vino de la nube.",
            moodEntryId = "mood-uuid-666",
            icon = "great_v2",
            createdAt = "2026-10-04T12:00:00Z"
        )

        val domain = dto.toDomain()

        assertEquals("note-uuid-5", domain.id)
        assertEquals("Sincronizado", domain.title)
        assertEquals("Vino de la nube.", domain.content)
        assertEquals("mood-uuid-666", domain.moodEntry)
        assertEquals("great_v2", domain.icon)
    }
}
