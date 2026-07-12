package com.nikolaevskii.lyte.feature.history.di

import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistoryViewModel
import kotlinx.datetime.TimeZone
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureHistoryModule = module {
    // WorkoutSessionRepository приходит из featureTrackerModule (история зависит от :feature:tracker:api).
    viewModel { HistoryViewModel(workoutSessionRepository = get(), timeZone = TimeZone.currentSystemDefault()) }
}
