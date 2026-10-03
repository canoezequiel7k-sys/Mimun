package com.canoezequiel.moodflow.data.repository

import com.canoezequiel.moodflow.data.local.dao.JournalEntryDao
import com.canoezequiel.moodflow.data.mapper.toDomain
import com.canoezequiel.moodflow.data.mapper.toDto
import com.canoezequiel.moodflow.data.mapper.toEntity
import com.canoezequiel.moodflow.data.mapper.toUpsertRequest
import com.canoezequiel.moodflow.data.remote.api.ApiClient
import com.canoezequiel.moodflow.domain.model.JournalEntry
import com.canoezequiel.moodflow.domain.repository.JournalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


//Implementacion en memoria del repositorio de notas(Fake para desarrollo local)
class JournalRepositoryImpl(
    private val dao: JournalEntryDao
) : JournalRepository {

    //Instancia del cliente Api de Retrofit
    private val api = ApiClient.apiService

    //Obtiene todas las notas ordenada desde la mas recienta a la mas antigua:
    override fun getAllJournalEntries(): List<JournalEntry> {
        return dao.getAllJournalEntries().map { it.toDomain() }
    }

    //Busca una nota especifica segun su ID
    override fun getJournalEntryById(id: String): JournalEntry? {
        return dao.getJournalEntryById(id)?.toDomain()
    }

    //Guarda una nueva nota o remplaza la existente si tiene el mismo ID (Edicion)
    override fun saveJournalEntry(entry: JournalEntry) {
        //Guardar primero en Romm(Fuente de la verdad local e inmediata)
        dao.insertJournalEntry(entry.toEntity())

        //Insertar el PUT upsert en el backend en segundo plano (Dispatcher.IO)
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val response = api.upsertJournalEntry(entry.id, entry.toUpsertRequest())
                if (!response.isSuccessful) {
                    // Manejo de error si el servidor rechaza la nota
                }
            }
        } catch (e: Exception){
            // Sin conexión: la nota persiste localmente en Room
        }
    }

    //Elimina una nota por su ID
    override fun deleteJournalEntry(id: String) {
        val nowIso = java.time.Instant.now().toString()
        // Borrado lógico local (Tombstone)
        dao.softDeleteJournalEntryById(id, nowIso)

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val response = api.deleteJournalEntry(id)
                if (response.isSuccessful) {
                    // Una vez confirmado por el servidor, se elimina físicamente de SQLite
                    dao.deleteJournalEntryPermanently(id)
                }
            }
        } catch (e: Exception) {
            // Sin conexión: la fila queda como PENDING_DELETE para ser enviada en la sincronización
        }
    }
}