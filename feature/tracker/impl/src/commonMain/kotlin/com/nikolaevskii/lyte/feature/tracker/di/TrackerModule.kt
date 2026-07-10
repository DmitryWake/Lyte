package com.nikolaevskii.lyte.feature.tracker.di

import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.TrackerLandingViewModel
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPickerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureTrackerModule = module {
    viewModelOf(::TrackerLandingViewModel)
    viewModelOf(::WorkoutPickerViewModel)
}
