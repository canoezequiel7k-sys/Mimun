package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.domain.model.NoteFilter
import com.canoezequiel.moodflow.ui.components.AddEditNoteDialog
import com.canoezequiel.moodflow.ui.components.DeleteConfirmDialog
import com.canoezequiel.moodflow.ui.components.JournalCard
import com.canoezequiel.moodflow.ui.components.NoteDetailDialog
import com.canoezequiel.moodflow.ui.theme.MimunBackground
import com.canoezequiel.moodflow.ui.theme.MimunGreen
import com.canoezequiel.moodflow.ui.theme.MimunTextPrimary
import com.canoezequiel.moodflow.ui.viewmodel.NotesViewModel

//Pantalla de reflexiones y diario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Back",
                        modifier = Modifier
                            .height(24.dp)
                            .width(24.dp)
                            .padding(start = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MimunBackground,
                    titleContentColor = MimunTextPrimary,
                    navigationIconContentColor = MimunGreen
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateForm() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(R.drawable.notes_screen_resuded),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize() //Ocupa todo el ancho y alto del Box
                    .alpha(0.5f),
                contentScale = ContentScale.Crop // <-- 2. Llena el espacio recortando excedentes sin deformar
            )
            // Columna con tus filtros y tu LazyColumn encima del fondo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (uiState.entries.isEmpty()) {
                    //Estado vacio cuando no hay reflexiones
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "\uD83D\uDCDD No reflections yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Press '+' to add a note to your journal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {

                    // Barra horizontal deslizante para los filtros (LazyRow)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedFilter == NoteFilter.ALL,
                                onClick = { viewModel.setFilter(NoteFilter.ALL) },
                                label = { Text("Todas") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.selectedFilter == NoteFilter.TODAY,
                                onClick = { viewModel.setFilter(NoteFilter.TODAY) },
                                label = { Text("Hoy") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.selectedFilter == NoteFilter.THIS_WEEK,
                                onClick = { viewModel.setFilter(NoteFilter.THIS_WEEK) },
                                label = { Text("Esta semana") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.selectedFilter == NoteFilter.THIS_MONTH,
                                onClick = { viewModel.setFilter(NoteFilter.THIS_MONTH) },
                                label = { Text("Este mes") }
                            )
                        }
                    }

                    //Lista de tarjetas de notas guardadas
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.entries, key = { it.id }) { entry ->
                            JournalCard(
                                entry = entry,
                                onCardClick = { viewModel.openDetail(entry) },
                                onEditClick = { viewModel.openEditForm(entry) },
                                onDeleteClick = { viewModel.requestDelete(entry) }
                            )
                        }
                    }
                }
            }
        }

        //Formulario model de creacion / Edicion
        if (uiState.isFormOpen) {
            AddEditNoteDialog(
                title = uiState.titleInput,
                content = uiState.contentInput,
                isEditing = uiState.editingEntry != null,
                onTitleChange = { viewModel.updateInputs(it, uiState.contentInput) },
                onContentChange = { viewModel.updateInputs(uiState.titleInput, it) },
                onDismiss = { viewModel.closeFrom() },
                onSave = { viewModel.saveEntry() }
            )
        }

        //Dialogo de confirmacion de eliminacion
        if (uiState.entryToDelete != null) {
            DeleteConfirmDialog(
                onConfirm = { viewModel.confirmDelete() },
                onDismiss = { viewModel.cancelDelete() }
            )
        }

        // Diálogo de lectura en pantalla completa / detalle de la nota
        if (uiState.selectedEntryDetail != null) {
            NoteDetailDialog(
                entry = uiState.selectedEntryDetail!!,
                onDismiss = { viewModel.closeDetail() },
                onEdit = {
                    viewModel.closeDetail()
                    viewModel.openEditForm(uiState.selectedEntryDetail!!)
                },
                onDelete = {
                    viewModel.closeDetail()
                    viewModel.requestDelete(uiState.selectedEntryDetail!!)
                }
            )
        }
    }
}