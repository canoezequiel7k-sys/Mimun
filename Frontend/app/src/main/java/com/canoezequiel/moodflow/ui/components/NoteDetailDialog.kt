package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.domain.model.JournalEntry
import java.time.format.DateTimeFormatter

@Composable
fun NoteDetailDialog(
    entry: JournalEntry,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Muestra el icono aleatorio en grande en la cabecera del detalle
                Image(
                    painter = painterResource(id = getNoteIconRes(entry.icon)),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fecha y hora detallada
                Text(
                    text = entry.timestamp.format(formatter),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Contenido completo de la nota sin recortes
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para editar
                TextButton(onClick = {
                    onDismiss()
                    onEdit()
                }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }

                // Botón para cerrar
                Button(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            // Botón para eliminar
            TextButton(
                onClick = {
                    onDismiss()
                    onDelete()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Eliminar")
            }
        }
    )
}