package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canoezequiel.moodflow.ui.theme.MimunGreen
import com.canoezequiel.moodflow.ui.theme.MimunSage
import com.canoezequiel.moodflow.ui.theme.MimunTextPrimary
import com.canoezequiel.moodflow.ui.theme.MimunTextSecondary

@Composable
fun MimunTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected) MimunGreen
                    else Color.Transparent
                )
                .padding(
                    horizontal = 24.dp,
                    vertical = 4.dp
                )
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = if (selected)
                    Color.White
                else
                    MimunTextSecondary
            )

        }
    }
}