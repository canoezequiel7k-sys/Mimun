package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.canoezequiel.moodflow.ui.theme.MimunBackground

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.fillMaxSize().background(MimunBackground)) {
        Text("LoginScreen")
    }
}