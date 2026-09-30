package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.domain.model.JournalEntry
import com.canoezequiel.moodflow.domain.repository.JournalRepository

//Caso de uso para guardar o modificar una nota
class SaveJournalEntryUseCase(
    private val repository: JournalRepository
) {
    operator fun invoke(entry: JournalEntry){
        repository.saveJournalEntry(entry)
    }
}