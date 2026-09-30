package com.canoezequiel.moodflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.canoezequiel.moodflow.MoodApplication
import com.canoezequiel.moodflow.data.local.database.AppDatabase
import com.canoezequiel.moodflow.data.repository.JournalRepositoryImpl
import com.canoezequiel.moodflow.domain.model.JournalEntry
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
    val isFormOpen: Boolean = false,
    val editingEntry: JournalEntry? = null,
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

        val entry = state.editingEntry?.copy(
            title = state.titleInput,
            content = state.contentInput
        ) ?: JournalEntry(
            title = state.titleInput,
            content = state.contentInput
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
}