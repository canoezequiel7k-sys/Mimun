package com.canoezequiel.moodflow.data.repository

import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.data.local.dao.MoodEntryDao
import com.canoezequiel.moodflow.data.mapper.toDomain
import com.canoezequiel.moodflow.data.mapper.toEntity
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import com.canoezequiel.moodflow.domain.repository.MoodRepository
import java.time.LocalDate
import java.time.LocalDateTime

//Implementa los contratos de domain. Decide CÓMO y DÓNDE se guardan las cosas
//Clase que implementa MoodRepository
class MoodRepositoryImpl(
    private val dao: MoodEntryDao
) : MoodRepository {

    //Define la lista física de las 6 emociones asociadas a los recursos gráficos
    override fun getAvailableMoods(): List<Mood> {
        return listOf(
            //RAD (5 frames)
            Mood(
                id = MoodType.RAD.name,
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

            //GOOD (6 frames)
            Mood(
                id = MoodType.GOOD.name,
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

            //MEH (Pendiente de agregar frames)
            Mood(
                id = MoodType.MEH.name,
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

            //Bad(Pendiente de agregar frames)
            Mood(
                id = MoodType.BAD.name,
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

            //AWFUL (Pendiente de agregar frames)
            Mood(
                id = MoodType.AWFUL.name,
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


    override fun saveMoodEntry(entry: MoodEntry) {
        val dateString = entry.timestamp.toLocalDate().toString()

        //Buscamos si ya existe un registro guardado localmente para el dia de hoy
        val existingEntity = dao.getMoodEntryByDate(dateString)

        //Si ya existe, PRESERVAMOS SU ID ORIGINAL. Si no, usamos el UUID nuevo
        val entityToSave = if (existingEntity != null){
            entry.copy(id = existingEntity.id).toEntity()
        } else {
            entry.toEntity()
        }

        // Guardamos en Room (fuente de verdad local)
        dao.deleteMoodEntriesByDate(dateString) // Borra cualquier registro viejo de hoy
        dao.insertMoodEntry(entityToSave)   // Inserta la nueva emoción elegida
    }

    //Compara la fecha actual (LocalDate.now()) con la fecha guardada en cada registro (it.timestamp.toLocalDate())
    override fun getTodayMoodEntry(): MoodEntry? {
        val dataString = LocalDate.now().toString()
        return dao.getMoodEntryByDate(dataString)?.toDomain()
    }

    override fun getAllMoodEntries(): List<MoodEntry> {
        return dao.getAllMoodEntries().map { it.toDomain() }
    }

}