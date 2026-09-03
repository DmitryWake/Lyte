package com.nikolaevskii.lyte.feature.onboarding.di

import com.nikolaevskii.lyte.feature.onboarding.presentation.viewmodel.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureOnboardingModule = module {
    viewModelOf(::OnboardingViewModel)
}
