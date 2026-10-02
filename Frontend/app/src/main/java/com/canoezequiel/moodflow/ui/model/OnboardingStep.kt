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
    OnboardingStep(R.drawable.ic_image_one, "How did you feel today?", "Track your emotions day by day."),
    OnboardingStep(R.drawable.ic_image_two, "Put your thoughts into words.", "Save what you want to remember."),
    OnboardingStep(R.drawable.ic_image_three, "Understand your journey.", "Observe your emotions over time.")
)
