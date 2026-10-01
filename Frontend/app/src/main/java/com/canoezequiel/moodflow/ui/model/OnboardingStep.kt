package com.canoezequiel.moodflow.ui.model

import com.canoezequiel.moodflow.R

//datos que se necesitan para cada Step
data class OnboardingStep(
    val imageRes: Int,
    val title: String,
    val description: String
)

//Y la lista (val steps) se define dentro de la pantalla o ViewModel de la UI
val onboardingSteps = listOf(
    OnboardingStep(R.drawable.ic_image_one, "¿Cómo te sentís hoy?", "Registrá tus emociones día a día"),
    OnboardingStep(R.drawable.ic_image_two, "Poné tus pensamientos en palabras.", " Guardá aquello que quieras recordar."),
    OnboardingStep(R.drawable.ic_image_three, "Entendé tu recorrido.", "Observá tus emociones a través del tiempo.")
)
