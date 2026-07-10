package com.nikolaevskii.lyte.feature.tracker.di

import com.nikolaevskii.lyte.feature.tracker.data.repository.WorkoutSessionRepositoryImpl
import com.nikolaevskii.lyte.feature.tracker.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.TrackerLandingViewModel
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPickerViewModel
import kotlin.time.Clock
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureTrackerModule = module {
    // WorkoutSessionDao приходит из coreDbModule().
    single<WorkoutSessionRepository> {
        WorkoutSessionRepositoryImpl(workoutSessionDao = get(), clock = Clock.System)
    }
    viewModelOf(::TrackerLandingViewModel)
    viewModelOf(::WorkoutPickerViewModel)
}
