package com.canoezequiel.moodflow.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.ui.theme.MoodFlowTheme

@Composable
fun SplashScreen() {
    MoodFlowTheme(){
        Scaffold(
        ) { innerPading ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPading),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.spash),
                    contentDescription = "Splash Image"
                )
            }
        }
    }
}

@Preview
@Composable
fun SplashPreview(

) {
    SplashScreen()
}