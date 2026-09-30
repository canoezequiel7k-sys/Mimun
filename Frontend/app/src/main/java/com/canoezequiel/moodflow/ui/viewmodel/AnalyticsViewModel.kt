package com.canoezequiel.moodflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.canoezequiel.moodflow.MoodApplication
import com.canoezequiel.moodflow.data.local.database.AppDatabase
import com.canoezequiel.moodflow.data.repository.MoodRepositoryImpl
import com.canoezequiel.moodflow.domain.model.AnalyticsSummary
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.domain.usecase.GetAnalyticsUseCase
import com.canoezequiel.moodflow.domain.usecase.GetMoodsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.YearMonth

// Estado de la interfaz de Analytics
data class AnalyticsUiState(
    val summary: AnalyticsSummary = AnalyticsSummary(),
    val availableMoods: List<Mood> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now()
)

// ViewModel encargado de gestionar el estado del calendario y estadísticas emocionales
class AnalyticsViewModel : ViewModel() {

    private val repository = MoodRepositoryImpl(
        AppDatabase.getInstance(MoodApplication.context).moodEntryDao()
    )

    private val getAnalyticsUseCase = GetAnalyticsUseCase(repository)
    private val getMoodsUseCase = GetMoodsUseCase(repository)

    private val _uiState = MutableStateFlow(
        AnalyticsUiState(
            summary = getAnalyticsUseCase(),
            availableMoods = getMoodsUseCase()
        )
    )

    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    // Carga las estadísticas directamente desde la base de datos local SQLite
    fun loadAnalytics() {
        _uiState.update {
            it.copy(summary = getAnalyticsUseCase())
        }
    }

    // Navega al mes anterior en el calendario
    fun previousMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.minusMonths(1))
        }
    }

    // Navega al mes siguiente en el calendario
    fun nextMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.plusMonths(1))
        }
    }
}