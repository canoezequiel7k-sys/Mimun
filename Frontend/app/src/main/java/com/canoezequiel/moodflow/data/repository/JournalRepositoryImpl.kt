package com.canoezequiel.moodflow.data.repository

import com.canoezequiel.moodflow.domain.model.JournalEntry
import com.canoezequiel.moodflow.domain.repository.JournalRepository


//Implementacion en memoria del repositorio de notas(Fake para desarrollo local)
class JournalRepositoryImpl : JournalRepository {

    private val entries = mutableListOf<JournalEntry>()

    //Obtiene todas las notas ordenada desde la mas recienta a la mas antigua:
    override fun getAllJournalEntries(): List<JournalEntry> {
        return entries.sortedByDescending { it.timestamp }
    }

    //Busca una nota especifica segun su ID
    override fun getJournalEntryById(id: String): JournalEntry? {
        return entries.find { it.id == id }
    }

    //Guarda una nueva nota o remplaza la existente si tiene el mismo ID (Edicion)
    override fun saveJournalEntry(entry: JournalEntry) {
        entries.removeAll {it.id == entry.id}
        entries.add(entry)
    }

    //Elimina una nota por su ID
    override fun deleteJournalEntry(id: String) {
        entries.removeAll{it.id == id}
    }
}