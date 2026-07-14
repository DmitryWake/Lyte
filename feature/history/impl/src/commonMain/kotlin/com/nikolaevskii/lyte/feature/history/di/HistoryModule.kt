package com.nikolaevskii.lyte.feature.history.di

import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistorySessionDetailsViewModel
import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHistoryModule = module {
    // WorkoutSessionRepository приходит из featureTrackerModule (история зависит от :feature:tracker:api).
    viewModelOf(::HistoryViewModel)
    viewModel { (sessionId: String) ->
        HistorySessionDetailsViewModel(
            sessionId = sessionId,
            workoutSessionRepository = get(),
            lyteNavigator = get(),
        )
    }
}
