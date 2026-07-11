package com.nikolaevskii.lyte.feature.tracker.di

import com.nikolaevskii.lyte.feature.tracker.data.repository.WorkoutSessionRepositoryImpl
import com.nikolaevskii.lyte.feature.tracker.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.TrackerLandingViewModel
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPickerViewModel
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPreviewViewModel
import kotlin.time.Clock
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureTrackerModule = module {
    // WorkoutSessionDao приходит из coreDbModule().
    single<WorkoutSessionRepository> {
        WorkoutSessionRepositoryImpl(workoutSessionDao = get(), clock = Clock.System)
    }
    viewModelOf(::TrackerLandingViewModel)
    viewModelOf(::WorkoutPickerViewModel)
    // WorkoutRepository приходит из featureWorkoutModule (трекер уже зависит от :feature:workout:api).
    viewModel { (programId: String) ->
        WorkoutPreviewViewModel(programId = programId, workoutRepository = get(), lyteNavigator = get())
    }
}
