package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canoezequiel.moodflow.ui.model.OnboardingStep
import com.canoezequiel.moodflow.ui.theme.MimunTextPrimary
import com.canoezequiel.moodflow.ui.theme.MimunTextSecondary

@Composable
fun OnBoardingPage(
    step: OnboardingStep
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = step.imageRes),
            contentDescription = step.title,
            modifier = Modifier.size(440.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = step.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MimunTextPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = step.description,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            color = MimunTextSecondary
        )

    }
}