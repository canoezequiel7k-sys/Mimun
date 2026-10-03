package com.canoezequiel.moodflow.data.repository

import com.canoezequiel.moodflow.data.local.dao.JournalEntryDao
import com.canoezequiel.moodflow.data.local.remote.api.ApiService
import com.canoezequiel.moodflow.data.mapper.toDomain
import com.canoezequiel.moodflow.data.mapper.toEntity
import com.canoezequiel.moodflow.data.mapper.toUpsertRequest
import com.canoezequiel.moodflow.data.remote.api.ApiClient
import com.canoezequiel.moodflow.domain.model.JournalEntry
import com.canoezequiel.moodflow.domain.repository.JournalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * [JournalRepositoryImpl]
 * QUÉ HACE: Implementación del repositorio de reflexiones (diario).
 * POR QUÉ: Gestiona la persistencia local en Room y las llamadas a la API REST.
 * CÓMO FUNCIONA: Guarda o realiza borrado lógico en Room y notifica asíncronamente a Retrofit.
 */
class JournalRepositoryImpl(
    private val dao: JournalEntryDao
) : JournalRepository {

    // Instancia perezosa de la API para evitar inicializar Context en entorno de pruebas unitarias
    private val api: ApiService by lazy { ApiClient.apiService }

    override fun getAllJournalEntries(): List<JournalEntry> {
        return dao.getAllJournalEntries().map { it.toDomain() }
    }

    override fun getJournalEntryById(id: String): JournalEntry? {
        return dao.getJournalEntryById(id)?.toDomain()
    }

    override fun saveJournalEntry(entry: JournalEntry) {
        dao.insertJournalEntry(entry.toEntity())

        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.upsertJournalEntry(entry.id, entry.toUpsertRequest())
                    if (response.isSuccessful) {
                        dao.markAsSynced(entry.id)
                    }
                } catch (_: Throwable) {
                    // Entorno offline o unit test
                }
            }
        } catch (_: Throwable) {
            // Entorno offline
        }
    }

    override fun deleteJournalEntry(id: String) {
        val nowIso = java.time.Instant.now().toString()
        dao.softDeleteJournalEntryById(id, nowIso)

        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.deleteJournalEntry(id)
                    if (response.isSuccessful) {
                        dao.deleteJournalEntryPermanently(id)
                    }
                } catch (_: Throwable) {
                    // Entorno offline
                }
            }
        } catch (_: Throwable) {
            // Entorno offline
        }
    }
}