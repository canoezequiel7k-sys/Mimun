package com.canoezequiel.moodflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.ui.components.OnBoardingPage
import com.canoezequiel.moodflow.ui.model.OnboardingStep
import com.canoezequiel.moodflow.ui.model.onboardingSteps
import com.canoezequiel.moodflow.ui.theme.MimunAccentDark
import com.canoezequiel.moodflow.ui.theme.MimunBackground
import com.canoezequiel.moodflow.ui.theme.MimunPrimaryContainer
import com.canoezequiel.moodflow.ui.theme.MimunSageLight
import com.canoezequiel.moodflow.ui.theme.MimunSurfaceVariant
import com.canoezequiel.moodflow.ui.theme.MoodFlowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinishOnboarding:() -> Unit
) {
    // Estado local para controlar la carga al presionar "Get Started"
    var isLoading by remember { mutableStateOf(false) }
    //Instanciamos una corrutina
    val coroutineScope = rememberCoroutineScope()
    //Pagina
    val pagerState = rememberPagerState(pageCount = { 3 })
    val isLastPage = pagerState.currentPage == 2 // o pagerState.pageCount - 1
    val buttonText = if(isLastPage) "Get Started" else "Continue"
    //Defines tus colores para cada pagina
    val targetColor = when (pagerState.currentPage){
        0 -> MimunAccentDark
        2 -> MimunSurfaceVariant
        else -> MimunSageLight
    }


    //Animacion suave del color
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500) // Duración de la transición
    )

    MoodFlowTheme() {
        Surface(
            color = animatedBackgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                //Horizontal pager para deslizar las imagenes y textos
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,

                ) { page ->
                    //Aca llamamos a tu componente visual y le pasamos el actual
                    val currentStep = onboardingSteps[page]
                    OnBoardingPage(step = currentStep)
                }

                Spacer(modifier = Modifier.height(24.dp))

                //Aqui repetimos tantas veces como pagina tenemos (3)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        //Verificamos si esta posicion es la pagina actual:
                        val isSelected = pagerState.currentPage == index

                        //Dibujemos cada punto (Box)
                        Box(
                            modifier = Modifier
                                // El activo puede ser un poquito más grande
                                .size(if (isSelected) 10.dp else 8.dp)
                                // Forma circular perfecta
                                .clip(CircleShape)
                                .background(
                                    // Color verde oliva activo (●)
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    // Color salvia más suave inactivo (○)
                                    else MaterialTheme.colorScheme.secondary
                                )
                                .clickable{}
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!isLastPage){
                            // Si no es la última página, avanza a la siguiente
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else if (!isLoading) {
                            // Si es la última página, mostramos el spinner de inmediato
                            isLoading = true
                            coroutineScope.launch {
                                delay(100) // Breve pausa para asegurar el renderizado del spinner
                                onFinishOnboarding()
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(text = buttonText)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun OnbordingPreview() {
    OnboardingScreen(
        onFinishOnboarding = {}
    )
}