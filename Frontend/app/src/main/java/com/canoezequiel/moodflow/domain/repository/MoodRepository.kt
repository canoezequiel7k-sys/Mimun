package com.canoezequiel.moodflow.domain.repository

import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.model.MoodEntry

//Define el contrato de datos.
interface MoodRepository {
    //Lista disponible de emociones.
    fun getAvailableMoods(): List<Mood>
    //Guarda el estado de animo.
    fun saveMoodEntry(entry: MoodEntry)
    //Obtener entrada de estado de ánimo de hoy.
    fun getTodayMoodEntry(): MoodEntry?
    //Registro de todos los estados de animo.
    fun getAllMoodEntries(): List<MoodEntry>
}