package com.canoezequiel.moodflow.ui.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Barra de navegación inferior con botón flotante central de acción (ADD)
@Composable
fun MoodBottomBar(
    onAddClicked: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier,
        actions = {},
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClicked() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
            }

        }
    )
}