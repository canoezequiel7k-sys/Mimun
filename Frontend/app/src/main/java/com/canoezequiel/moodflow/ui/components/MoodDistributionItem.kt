package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.domain.model.Mood
import com.canoezequiel.moodflow.ui.theme.MimunSurface

// Elemento individual para mostrar la barra de porcentaje de cada emoción
@Composable
fun MoodDistributionItem(
    mood: Mood, // <- Importación correcta del modelo de dominio
    count: Int,
    percentage: Float
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MimunSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = mood.iconRes), // Usa 'iconRes'
                contentDescription = mood.name,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mood.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "$count days (${String.format("%.1f", percentage)}%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}