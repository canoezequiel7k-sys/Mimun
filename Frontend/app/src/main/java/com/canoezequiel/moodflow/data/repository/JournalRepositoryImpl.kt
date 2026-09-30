package com.canoezequiel.moodflow.data.repository

import com.canoezequiel.moodflow.data.local.dao.JournalEntryDao
import com.canoezequiel.moodflow.data.mapper.toDomain
import com.canoezequiel.moodflow.data.mapper.toEntity
import com.canoezequiel.moodflow.domain.model.JournalEntry
import com.canoezequiel.moodflow.domain.repository.JournalRepository


//Implementacion en memoria del repositorio de notas(Fake para desarrollo local)
class JournalRepositoryImpl(
    private val dao: JournalEntryDao
) : JournalRepository {


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
        dao.insertJournalEntry(entry.toEntity())
    }

    //Elimina una nota por su ID
    override fun deleteJournalEntry(id: String) {
        dao.deleteJournalEntryById(id)
    }
}