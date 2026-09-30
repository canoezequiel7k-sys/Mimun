package com.canoezequiel.moodflow.domain.repository

import com.canoezequiel.moodflow.domain.model.JournalEntry

//Interfaz que define las operaciones de datos para las reflexiones/notas
interface JournalRepository{
    fun getAllJournalEntries(): List<JournalEntry>
    fun getJournalEntryById(id: String): JournalEntry?
    fun saveJournalEntry(entry: JournalEntry)
    fun deleteJournalEntry(id: String)
}