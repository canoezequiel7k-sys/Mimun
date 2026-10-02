package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MimunButton(
    text: String,
    image: ImageVector,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {},
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = image,
            contentDescription = text,
        )
    }
}