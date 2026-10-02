package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.ui.components.AdditionalIncome
import com.canoezequiel.moodflow.ui.components.MimunButton
import com.canoezequiel.moodflow.ui.components.MimunTextField
import com.canoezequiel.moodflow.ui.components.titleAndImage
import com.canoezequiel.moodflow.ui.theme.MimunBackground
import com.canoezequiel.moodflow.ui.theme.MimunTextPrimary
import com.canoezequiel.moodflow.ui.theme.MimunTextSecondary
import com.canoezequiel.moodflow.ui.theme.MoodFlowTheme
import com.canoezequiel.moodflow.ui.theme.TESTCOLOR
import com.canoezequiel.moodflow.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated){
            onLoginSuccess()
        }
    }

    MoodFlowTheme{
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {Text("")},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MimunBackground
                    )
                )
            },
            containerColor = MimunBackground,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MimunBackground)
            ) {
                Image(
                    painter = painterResource(
                        R.drawable.ic_one_p
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 40.dp, y = 56.dp),
                    alpha = 0.4f
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    titleAndImage(
                        title = "Welcome back",
                        description = "Good to see you again. Take a moment for yourself.",
                        image = R.drawable.welcome_back,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    MimunTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        labelText = "Email",
                        placeHolderText = "Insert your email",
                        isPassword = false,
                        iconOT = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MimunTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        labelText = "Password",
                        placeHolderText = "Insert your password",
                        isPassword = true,
                        iconOT = Icons.Default.Lock,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    MimunButton(
                        text = "Log in",
                        image = Icons.Default.ArrowRightAlt,
                        onClick = {viewModel.login()},
                        isLoading = uiState.isLoading,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    AdditionalIncome(
                        title = "Don't have an account?",
                        textButton = "Sign up",
                        accionBurron = {onNavigateToRegister()},
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}