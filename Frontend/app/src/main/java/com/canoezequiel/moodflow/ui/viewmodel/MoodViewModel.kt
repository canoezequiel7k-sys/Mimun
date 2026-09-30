package com.canoezequiel.moodflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.canoezequiel.moodflow.MoodApplication
import com.canoezequiel.moodflow.data.local.database.AppDatabase
import com.canoezequiel.moodflow.data.repository.MoodRepositoryImpl
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.model.MoodEntry
import com.canoezequiel.moodflow.domain.model.MoodType
import com.canoezequiel.moodflow.domain.usecase.GetMoodsUseCase
import com.canoezequiel.moodflow.domain.usecase.GetTodayMoodEntryUseCase
import com.canoezequiel.moodflow.domain.usecase.SaveMoodEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class MoodUiState(
    val moods: List<Mood> = emptyList(),
    val selectedMood: Mood? = null,
    val noteText: String = "",
    val todayEntry: MoodEntry? = null,
    val showSuccessMessage: Boolean = false
)

//ViewModel encagado de administrar el estado de la interfaz y la logica de seleccion
class MoodViewModel : ViewModel() {

    //Instancia del repositorio y caso de uso(Sin DI por ahora, para mantenerlo simple)
    private val repository = MoodRepositoryImpl(
        AppDatabase.getInstance(MoodApplication.context).moodEntryDao()
    )

    //Conexion con capa de dominio
    private val getMoodsUseCase = GetMoodsUseCase(repository)
    private val saveMoodEntryUseCase = SaveMoodEntryUseCase(repository)
    private val getTodayMoodEntryUseCase = GetTodayMoodEntryUseCase(repository)

    //Expone el estado inmutable uiState StateFlow<MoodUiState> a la UI.
    private val _uiState = MutableStateFlow(
        MoodUiState(
            moods = getMoodsUseCase(),
            todayEntry = getTodayMoodEntryUseCase()
        )
    )

//    Lista inmutable de humores expuesta a la UI
//    val moods: List<Mood> = getMoodsUseCase()
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    //Metodo para seleccionar un estado de animo
    fun selectMood(mood: Mood) {
        _uiState.update { it.copy(selectedMood = mood, showSuccessMessage = false) }
    }

    //Metodo para actualizar la nota de texto
    fun updateNoteText(note: String){
        _uiState.update { it.copy(noteText = note) }
    }

    //Metodo para guardar el estado de animo de hoy
    fun saveTodayMood() {
        val selected = _uiState.value.selectedMood ?: return
        val moodType = try {
            MoodType.valueOf(selected.id)
        } catch (e: Exception) {
            MoodType.GOOD
        }

        val entry = MoodEntry(
            moodType = moodType,
            note = _uiState.value.noteText.ifBlank { null }
        )

        saveMoodEntryUseCase(entry)

        _uiState.update {
            it.copy(
                todayEntry = entry,
                showSuccessMessage = true
            )
        }
    }
}