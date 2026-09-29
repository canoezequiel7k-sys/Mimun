package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.repository.MoodRepository

class SaveMoodEntryUseCase (
    private val repository: MoodRepository
){
    operator fun invoke(entry: MoodEntry){
        repository.saveMoodEntry(entry)
    }
}