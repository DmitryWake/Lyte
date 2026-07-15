package com.nikolaevskii.lyte.feature.tracker.di

import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.ActiveSessionViewModel
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.TrackerLandingViewModel
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPickerViewModel
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPreviewViewModel
import kotlin.time.Clock
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// WorkoutSessionRepository регистрирует coreSessionModule(); WorkoutRepository — coreWorkoutModule().
val featureTrackerModule = module {
    viewModelOf(::TrackerLandingViewModel)
    viewModelOf(::WorkoutPickerViewModel)
    // WorkoutRepository приходит из featureWorkoutModule (трекер уже зависит от :feature:workout:api).
    viewModel { (programId: String) ->
        WorkoutPreviewViewModel(
            programId = programId,
            workoutRepository = get(),
            workoutSessionRepository = get(),
            lyteNavigator = get(),
        )
    }
    viewModel { (sessionId: String) ->
        ActiveSessionViewModel(
            sessionId = sessionId,
            workoutSessionRepository = get(),
            lyteNavigator = get(),
            clock = Clock.System,
        )
    }
}
