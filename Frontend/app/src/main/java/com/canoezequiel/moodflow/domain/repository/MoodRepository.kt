package com.canoezequiel.moodflow.domain.repository

import com.canoezequiel.moodflow.domain.model.Mood

interface MoodRepository {
    fun getModel(): List<Mood>
}