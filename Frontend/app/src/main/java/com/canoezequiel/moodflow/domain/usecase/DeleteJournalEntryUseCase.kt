package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.domain.repository.JournalRepository

//Caso de uso para eliminar una nota por su ID
class DeleteJournalEntryUseCase (
    private val repository: JournalRepository
){
    operator fun invoke(id: String){
        repository.deleteJournalEntry(id)
    }
}