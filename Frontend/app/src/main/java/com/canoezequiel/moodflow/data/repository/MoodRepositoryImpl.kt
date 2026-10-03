package com.canoezequiel.moodflow.data.repository

import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.data.local.dao.MoodEntryDao
import com.canoezequiel.moodflow.data.mapper.toDomain
import com.canoezequiel.moodflow.data.mapper.toEntity
import com.canoezequiel.moodflow.data.mapper.toUpsertRequest
import com.canoezequiel.moodflow.data.remote.api.ApiClient
import com.canoezequiel.moodflow.data.local.remote.api.ApiService
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import com.canoezequiel.moodflow.domain.repository.MoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

/**
 * [MoodRepositoryImpl]
 * QUÉ HACE: Implementación del repositorio de estados de ánimo.
 * POR QUÉ: Administra el almacenamiento local inmediato en Room y la sincronización asíncrona con el backend.
 * CÓMO FUNCIONA: Guarda en SQLite preservando el ID si la fecha ya existe, e intenta enviar el upsert vía Retrofit.
 */
class MoodRepositoryImpl(
    private val dao: MoodEntryDao
) : MoodRepository {

    // Instancia perezosa de la API para evitar inicializar Context en entorno de pruebas unitarias
    private val api: ApiService by lazy { ApiClient.apiService }

    override fun getAvailableMoods(): List<Mood> {
        return listOf(
            Mood(
                id = MoodType.RAD.name,
                name = "rad",
                iconRes = R.drawable.rad1,
                frames = listOf(
                    R.drawable.rad1,
                    R.drawable.rad2,
                    R.drawable.rad3,
                    R.drawable.rad4,
                    R.drawable.rad5
                ),
                colorHex = "#88965C"
            ),
            Mood(
                id = MoodType.GOOD.name,
                name = "good",
                iconRes = R.drawable.good5,
                frames = listOf(
                    R.drawable.good1,
                    R.drawable.good2,
                    R.drawable.good3,
                    R.drawable.good4,
                    R.drawable.good5,
                    R.drawable.good6
                ),
                colorHex = "#8DA08D",
                iconSize = 68.dp
            ),
            Mood(
                id = MoodType.MEH.name,
                name = "meh",
                iconRes = R.drawable.meh1,
                frames = listOf(
                    R.drawable.meh1,
                    R.drawable.meh2,
                    R.drawable.meh3,
                    R.drawable.meh4,
                    R.drawable.meh5,
                    R.drawable.meh6
                ),
                colorHex = "#D8DBCC"
            ),
            Mood(
                id = MoodType.BAD.name,
                name = "bad",
                iconRes = R.drawable.bad2,
                frames = listOf(
                    R.drawable.bad1,
                    R.drawable.bad2,
                    R.drawable.bad3,
                    R.drawable.bad4,
                    R.drawable.bad5,
                    R.drawable.bad6
                ),
                colorHex = "#F4CC9B"
            ),
            Mood(
                id = MoodType.AWFUL.name,
                name = "awful",
                iconRes = R.drawable.awful3,
                frames = listOf(
                    R.drawable.awful1,
                    R.drawable.awful2,
                    R.drawable.awful3,
                    R.drawable.awful4,
                    R.drawable.awful5,
                    R.drawable.awful6
                ),
                colorHex = "#E6B77F"
            )
        )
    }

    override fun saveMoodEntry(entry: MoodEntry) {
        val dateString = entry.timestamp.toLocalDate().toString()

        val existingEntity = dao.getMoodEntryByDate(dateString)

        // El backend solo acepta UUID. Conservamos el id del día si ya es un UUID válido;
        // si no (registros viejos como "mood_entry_2026-10-03"), generamos uno nuevo.
        val isUuid = { value: String -> runCatching { UUID.fromString(value) }.isSuccess }
        val stableId = existingEntity?.id?.takeIf(isUuid)
            ?: entry.id.takeIf(isUuid)
            ?: UUID.randomUUID().toString()
        val entryToSave = entry.copy(id = stableId)

        // Si el id cambió, quitamos la fila vieja para no duplicar el día (nunca llegó a sincronizarse).
        if (existingEntity != null && existingEntity.id != stableId) {
            dao.deleteMoodEntryPermanently(existingEntity.id)
        }

        dao.insertMoodEntry(entryToSave.toEntity())

        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.upsertMoodEntry(entryToSave.id, entryToSave.toUpsertRequest())
                    if (response.isSuccessful) {
                        dao.markAsSynced(entryToSave.id)
                    }
                } catch (_: Throwable) {
                    // Entorno offline o unit test
                }
            }
        } catch (_: Throwable) {
            // Entorno offline
        }
    }

    override fun getTodayMoodEntry(): MoodEntry? {
        val dataString = LocalDate.now().toString()
        return dao.getMoodEntryByDate(dataString)?.toDomain()
    }

    override fun getAllMoodEntries(): List<MoodEntry> {
        return dao.getAllMoodEntries().map { it.toDomain() }
    }
}