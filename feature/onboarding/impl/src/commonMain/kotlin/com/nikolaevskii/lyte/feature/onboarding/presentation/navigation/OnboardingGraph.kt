package com.nikolaevskii.lyte.feature.onboarding.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.nikolaevskii.lyte.feature.onboarding.OnboardingRoute
import com.nikolaevskii.lyte.feature.onboarding.presentation.screen.OnboardingScreen

fun NavGraphBuilder.onboardingGraph() {
    composable<OnboardingRoute> {
        OnboardingScreen()
    }
}
