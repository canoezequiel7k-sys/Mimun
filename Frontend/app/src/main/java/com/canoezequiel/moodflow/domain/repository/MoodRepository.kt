package com.canoezequiel.moodflow.domain.repository

import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.model.MoodEntry

interface MoodRepository {
    fun getAvailableMoods(): List<Mood>
    fun saveMoodEntry(entry: MoodEntry)
    fun getTodayMoodEntry(): MoodEntry?
    fun getAllMoodEntries(): List<MoodEntry>
}