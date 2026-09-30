package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.repository.MoodRepository

//Caso de uso que recibe un MoodEntry y solicita guardarlo.
class SaveMoodEntryUseCase (
    private val repository: MoodRepository
){
    operator fun invoke(entry: MoodEntry){
        repository.saveMoodEntry(entry)
    }
}