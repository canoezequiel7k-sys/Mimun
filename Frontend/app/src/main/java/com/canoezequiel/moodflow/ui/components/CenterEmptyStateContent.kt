package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//Área central descriptiva con guía para el usuario.
@Composable
fun CenterEmptyStateContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Espacio reservado para tu imagen central futura
        Text(text = "🖼️ [Future Center Image]", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje guía
        Text(
            text = "Let's add the first entry! tap the big PLUS button",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Indicador hacia el botón central inferior
        Text(text = "👇", style = MaterialTheme.typography.headlineMedium)
    }
}