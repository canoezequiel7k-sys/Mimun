package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.domain.model.JournalEntry
import com.canoezequiel.moodflow.domain.repository.JournalRepository

//Caso de uso para obtener todas las notas guardadas
class GetJournalEntriesUseCase(
    private val repository: JournalRepository
) {
    operator fun invoke(): List<JournalEntry> {
        return repository.getAllJournalEntries()
    }
}