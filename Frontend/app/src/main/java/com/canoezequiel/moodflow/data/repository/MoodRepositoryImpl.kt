package com.canoezequiel.moodflow.data.repository

import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.repository.MoodRepository

class MoodRepositoryImpl : MoodRepository {
    override fun getModel(): List<Mood> {
        return listOf(
            // 1. RAD (5 frames)
            Mood(
                id = "1",
                name = "rad",
                iconRes = R.drawable.rad1,
                frames = listOf(
                    R.drawable.rad1,
                    R.drawable.rad2,
                    R.drawable.rad3,
                    R.drawable.rad4,
                    R.drawable.rad5
                ),
                colorHex = "#4CAF50"
            ),

            // 2. GOOD (6 frames)
            Mood(
                id = "2",
                name = "good",
                iconRes = R.drawable.good5,
                frames = listOf(
                    R.drawable.good1,
                    R.drawable.good2,
                    R.drawable.good3,
                    R.drawable.good4,
                    R.drawable.good5,
                    R.drawable.good6
                ),
                colorHex = "#8BC34A",
                iconSize = 68.dp
            ),

            // 3. MEH (Pendiente de agregar frames)
            Mood(
                id = "3",
                name = "meh",
                iconRes = R.drawable.meh1, // Reemplazar cuando tengas sus drawables
                frames = listOf(
                    R.drawable.meh1,
                    R.drawable.meh2,
                    R.drawable.meh3,
                    R.drawable.meh4,
                    R.drawable.meh5,
                    R.drawable.meh6
                ),
                colorHex = "#FFC107"
            ),

            // 4. BAD (Pendiente de agregar frames)
            Mood(
                id = "4",
                name = "bad",
                iconRes = R.drawable.bad2, // Reemplazar cuando tengas sus drawables
                frames = listOf(
                    R.drawable.bad1,
                    R.drawable.bad2,
                    R.drawable.bad3,
                    R.drawable.bad4,
                    R.drawable.bad5,
                    R.drawable.bad6
                ),
                colorHex = "#FF9800"
            ),

            // 5. AWFUL (Pendiente de agregar frames)
            Mood(
                id = "5",
                name = "awful",
                iconRes = R.drawable.awful3, // Reemplazar cuando tengas sus drawables
                frames = listOf(
                    R.drawable.awful1,
                    R.drawable.awful2,
                    R.drawable.awful3,
                    R.drawable.awful4,
                    R.drawable.awful5,
                    R.drawable.awful6
                ),
                colorHex = "#F44336"
            )
        )
    }
}