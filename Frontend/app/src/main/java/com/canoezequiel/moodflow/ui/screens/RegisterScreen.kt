package com.canoezequiel.moodflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.ui.components.AdditionalIncome
import com.canoezequiel.moodflow.ui.components.MimunButton
import com.canoezequiel.moodflow.ui.components.MimunTextField
import com.canoezequiel.moodflow.ui.components.titleAndImage
import com.canoezequiel.moodflow.ui.theme.MimunBackground
import com.canoezequiel.moodflow.ui.theme.MimunTextSecondary
import com.canoezequiel.moodflow.ui.theme.MoodFlowTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit
) {
    MoodFlowTheme{
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {Text("")},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MimunBackground
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = {},
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MimunTextSecondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Back"
                            )
                        }
                    }
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
                        R.drawable.ic_two_p
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-40).dp, y = 38.dp),
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
                        title = "Create your account",
                        description = "Start your journey. Your emotions matter.",
                        image = R.drawable.create_your_account,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    MimunTextField(
                        value = "",
                        onValueChange = {},
                        labelText = "Full name",
                        placeHolderText = "John Doe",
                        isPassword = false,
                        iconOT = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MimunTextField(
                        value = "",
                        onValueChange = {},
                        labelText = "Email",
                        placeHolderText = "your@email.com",
                        isPassword = false,
                        iconOT = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MimunTextField(
                        value = "",
                        onValueChange = {},
                        labelText = "Password",
                        placeHolderText = "Create a password",
                        isPassword = true,
                        iconOT = Icons.Default.Lock,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    MimunTextField(
                        value = "",
                        onValueChange = {},
                        labelText = "Confirm password",
                        placeHolderText = "Repeat your password",
                        isPassword = true,
                        iconOT = Icons.Default.Lock,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    MimunButton(
                        text = "Create account",
                        image = Icons.Default.ArrowRightAlt,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    AdditionalIncome(
                        title = "Already have an account?",
                        textButton = "Log in",
                        accionBurron = {onNavigateToLogin()},
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}