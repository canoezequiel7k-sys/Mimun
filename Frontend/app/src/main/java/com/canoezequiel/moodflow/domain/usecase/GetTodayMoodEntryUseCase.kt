package com.canoezequiel.moodflow.domain.usecase

import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.repository.MoodRepository

//Caso de uso que consulta si ya existe un registro creado hoy
class GetTodayMoodEntryUseCase (
    private val repository: MoodRepository
){
    operator fun invoke(): MoodEntry? {
        return repository.getTodayMoodEntry()
    }
}