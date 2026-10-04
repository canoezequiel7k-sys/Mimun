package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.domain.model.JournalEntry
import com.canoezequiel.moodflow.ui.theme.MimunSurface
import java.time.format.DateTimeFormatter

// Componente visual de tarjeta individual para una nota/reflexión del diario
@Composable
fun JournalCard(
    entry: JournalEntry,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Formato de fecha solo día/mes/año sin hora
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }, //se abre la edición al hacer clic
        colors = CardDefaults.cardColors(
            containerColor = MimunSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Mantiene separados la izquierda de la derecha
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- BLOQUE IZQUIERDO (Icono + Título truncado) ---
                Row(
                    modifier = Modifier.weight(1f), // Ocupa el espacio flexible pero deja lugar a los botones
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre el icono y el título
                ) {
                    Image(
                        painter = painterResource(id = getNoteIconRes(entry.icon)),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,                              // Obliga a que sea una sola línea
                        overflow = TextOverflow.Ellipsis           // Agrega "..." si el texto es muy largo
                    )
                }

                // --- BLOQUE DERECHO (Botones fijos) ---
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = entry.timestamp.format(formatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ------------------------------------------------------------------------//
// Función auxiliar para convertir el texto del icono a su recurso gráfico //
// ------------------------------------------------------------------------//
fun getNoteIconRes(iconName: String?): Int {
    return when (iconName) {
        "apple" -> R.drawable.apple
        "great_v2" -> R.drawable.great_v2
        "sky" -> R.drawable.sky
        "sun" -> R.drawable.sun
        else -> R.drawable.sun // Icono por defecto de respaldo
    }
}