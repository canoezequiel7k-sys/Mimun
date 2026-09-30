package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.ui.components.AddEditNoteDialog
import com.canoezequiel.moodflow.ui.components.DeleteConfirmDialog
import com.canoezequiel.moodflow.ui.components.JournalCard
import com.canoezequiel.moodflow.ui.viewmodel.NotesViewModel

//Pantalla de reflexiones y diario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reflection Journal") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {viewModel.openCreateForm()},
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.entries.isEmpty()){
                //Estado vacio cuando no hay reflexiones
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\uD83D\uDCDD Sin reflexiones aún",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Presiona '+' para agregar una nota a tu diario",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                //Lista de tarjetas de notas guardadas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.entries, key = { it.id }) {entry ->
                        JournalCard(
                            entry = entry,
                            onEditClick = {viewModel.openEditForm(entry)},
                            onDeleteClick = {viewModel.requestDelete(entry)}
                        )
                    }
                }
            }
        }

        //Formulario model de creacion / Edicion
        if (uiState.isFormOpen){
            AddEditNoteDialog(
                title = uiState.titleInput,
                content = uiState.contentInput,
                isEditing = uiState.editingEntry != null,
                onTitleChange = {viewModel.updateInputs(it, uiState.contentInput)},
                onContentChange = {viewModel.updateInputs(uiState.titleInput, it)},
                onDismiss = {viewModel.closeFrom()},
                onSave = {viewModel.saveEntry()}
            )
        }

        //Dialogo de confirmacion de eliminacion
        if (uiState.entryToDelete != null){
            DeleteConfirmDialog(
                onConfirm = {viewModel.confirmDelete()},
                onDismiss = {viewModel.cancelDelete()}
            )
        }
    }
}