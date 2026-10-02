package com.canoezequiel.moodflow.ui.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.ui.theme.MimunTextPrimary
import com.canoezequiel.moodflow.ui.theme.MimunTextSecondary

@Composable
fun titleAndImage(
    title: String,
    description: String,
    image: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 34.sp,
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.titleLarge,
                color = MimunTextPrimary
            )
            Text(
                text = description,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodySmall,
                color = MimunTextSecondary
            )
        }
        Image(
            painter = painterResource(image),
            contentDescription = title,
            modifier = Modifier.weight(1f)
        )
    }
}

//@Preview
//@Composable
//fun PreviewIns(
//
//) {
//    titleAndImage(
//        title = "Welcome back",
//        description = "Good to see you again. Take a moment for yourself.",
//        image = R.drawable.welcome_back
//    )
//
//}