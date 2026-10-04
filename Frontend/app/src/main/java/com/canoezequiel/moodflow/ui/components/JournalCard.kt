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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
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
    onCardClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }, // Al tocar la tarjeta, abre el detalle completo
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp) // Bordes suaves como en la referencia
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            //ICONO ALEATORIO IZQUIERDO
            Image(
                painter = painterResource(id = getNoteIconRes(entry.icon)),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )

            //COLUMNA CENTRAL (Fecha, Título y Contenido)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Fecha formateada
                Text(
                    text = entry.timestamp.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // Título de la nota
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Contenido de la nota (recortado a 2 líneas para la tarjeta)
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            //Icono del estado de ánimo a la derecha (RAD, GOOD, etc.)
            val moodRes = getMoodIconRes(entry.emoji) // Leemos 'entry.emoji'
            if (moodRes != null) {
                Image(
                    painter = painterResource(id = moodRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
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

// Función auxiliar para obtener el icono de la emoción
fun getMoodIconRes(moodTypeStr: String?): Int? {
    return when (moodTypeStr?.uppercase()) {
        "RAD" -> R.drawable.rad8
        "GOOD" -> R.drawable.good7
        "MEH" -> R.drawable.meh4
        "BAD" -> R.drawable.bad6
        "AWFUL" -> R.drawable.awful4
        else -> null
    }
}
