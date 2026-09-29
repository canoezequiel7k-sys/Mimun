package com.canoezequiel.moodflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.canoezequiel.moodflow.data.repository.MoodRepositoryImpl
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.usecase.GetMoodsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


//ViewModel encagado de administrar el estado de la interfaz y la logica de seleccion
class MoodViewModel : ViewModel() {

    //Instancia del repositorio y caso de uso(Sin DI por ahora, para mantenerlo simple)
    private val repository = MoodRepositoryImpl()
    private val getMoodsUseCase = GetMoodsUseCase(repository)

    //Lista inmutable de humores expuesta a la UI
    val moods: List<Mood> = getMoodsUseCase()

    //Estado interno mutable para el humo seleccionado
    private val _selectedMood = MutableStateFlow<Mood?>(null)

    //Estado inmutable observable por la UI mediante StateFlow
    val selectedMood: StateFlow<Mood?> = _selectedMood.asStateFlow()

    //Accion para actualizar el humor seleccionado
    fun selectMood(mood: Mood){
        _selectedMood.value = mood
    }
}