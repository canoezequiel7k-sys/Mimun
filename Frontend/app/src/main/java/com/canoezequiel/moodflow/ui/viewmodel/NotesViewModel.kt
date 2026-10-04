package com.canoezequiel.moodflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.canoezequiel.moodflow.MoodApplication
import com.canoezequiel.moodflow.data.local.database.AppDatabase
import com.canoezequiel.moodflow.data.repository.JournalRepositoryImpl
import com.canoezequiel.moodflow.domain.model.JournalEntry
import com.canoezequiel.moodflow.domain.model.NoteFilter
import com.canoezequiel.moodflow.domain.usecase.DeleteJournalEntryUseCase
import com.canoezequiel.moodflow.domain.usecase.GetJournalEntriesUseCase
import com.canoezequiel.moodflow.domain.usecase.SaveJournalEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

//Estado inmutable de la pantalla de notas
data class NotesUiState(
    val entries: List<JournalEntry> = emptyList(),
    val selectedFilter: NoteFilter = NoteFilter.ALL,
    val isFormOpen: Boolean = false,
    val editingEntry: JournalEntry? = null,
    val selectedEntryDetail: JournalEntry? = null, // Nota seleccionada para ver en detalle
    val titleInput: String = "",
    val contentInput: String = "",
    val entryToDelete: JournalEntry? = null
)

//ViewModel encargado de la gestion del estado de la interfaz de nota
class NotesViewModel : ViewModel() {

    // Instancia del repositorio pasando el DAO de Room
    private val repository = JournalRepositoryImpl(
        AppDatabase.getInstance(MoodApplication.context).journalEntryDao()
    )
    private val getJournalEntriesUseCase = GetJournalEntriesUseCase(repository)
    private val saveJournalEntryUseCase = SaveJournalEntryUseCase(repository)
    private val deleteJournalEntryUseCase = DeleteJournalEntryUseCase(repository)

    private val _uiState = MutableStateFlow(NotesUiState(entries = getJournalEntriesUseCase()))
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()


    //Abre el formulario para crea una nueva nota
    fun openCreateForm(){
        _uiState.update {
            it.copy(
                isFormOpen = true,
                editingEntry = null,
                titleInput = "",
                contentInput = ""
            )
        }
    }

    // Abre el formulario cargando los datos de una nota existente para editarla
    fun openEditForm(entry: JournalEntry) {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                editingEntry = entry,
                titleInput = entry.title,
                contentInput = entry.content
            )
        }
    }

    //Cierra el formulario de creacion-edicion
    fun closeFrom(){
        _uiState.update {
            it.copy(
                isFormOpen = false,
                editingEntry = null
            )
        }
    }

    //Actualiza los campos de entrada del formulario
    fun updateInputs(title: String, content: String){
        _uiState.update {
            it.copy(
                titleInput = title,
                contentInput = content
            )
        }
    }

    //Guarda una nota nueva o actualizada y recarga la lista
    fun saveEntry() {
        val state = _uiState.value

        if(state.titleInput.isBlank() || state.contentInput.isBlank()) return

        //Definimos nuestra lista de iconos disponibles en formato String
        val availableIcons = listOf("apple", "great_v2", "sky", "sun")

        //Si es edición, mantenemos la nota que ya existía. Si es nueva, la creamos con un icono aleatorio.
        val entry = state.editingEntry?.copy(
            title = state.titleInput,
            content = state.contentInput
        ) ?: JournalEntry(
            title = state.titleInput,
            content = state.contentInput,
            icon = availableIcons.random() // Aquí asignamos el icono al azar al crear una nota nueva!
        )

        saveJournalEntryUseCase(entry)
        _uiState.update {
            it.copy(
                entries = getJournalEntriesUseCase(),
                isFormOpen = false,
                editingEntry = null
            )
        }
    }


    //Solicita confirmacion antes de borrar una nota
    fun requestDelete(entry: JournalEntry){
        _uiState.update { it.copy(entryToDelete = entry) }
    }

    //Cancele la solicitud de borrado
    fun cancelDelete() {
        _uiState.update { it.copy(entryToDelete = null) }
    }

    //Confirma la eliminacion de la nota
    fun confirmDelete(){
        val id = _uiState.value.entryToDelete?.id ?: return

        deleteJournalEntryUseCase(id)
        _uiState.update {
            it.copy(
                entries = getJournalEntriesUseCase(),
                entryToDelete = null
            )
        }
    }

    // Abre la vista de detalle en pantalla completa de una nota
    fun openDetail(entry: JournalEntry) {
        _uiState.update { it.copy(selectedEntryDetail = entry) }
    }

    // Cierra la vista de detalle
    fun closeDetail() {
        _uiState.update { it.copy(selectedEntryDetail = null) }
    }

    // Cambia el filtro activo y filtra la lista de notas en tiempo real
    fun setFilter(filter: NoteFilter) {
        val allEntries = getJournalEntriesUseCase()

        val filteredEntries = when (filter) {
            NoteFilter.ALL -> allEntries
            NoteFilter.TODAY -> {
                val today = java.time.LocalDate.now()
                allEntries.filter { it.timestamp.toLocalDate() == today }
            }
            NoteFilter.THIS_WEEK -> {
                val now = java.time.LocalDate.now()
                // Filtra las notas cuya fecha esté dentro de los últimos 7 días
                allEntries.filter { !it.timestamp.toLocalDate().isBefore(now.minusDays(7)) }
            }
            NoteFilter.THIS_MONTH -> {
                val now = java.time.LocalDate.now()
                allEntries.filter {
                    it.timestamp.year == now.year && it.timestamp.month == now.month
                }
            }
        }

        _uiState.update {
            it.copy(
                selectedFilter = filter,
                entries = filteredEntries
            )
        }
    }

}