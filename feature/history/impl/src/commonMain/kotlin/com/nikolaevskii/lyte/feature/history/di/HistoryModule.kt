package com.nikolaevskii.lyte.feature.history.di

import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistorySessionDetailsViewModel
import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.Clock

// SessionHistoryRepository (read-контракт) приходит из coreSessionModule().
val featureHistoryModule = module {
    viewModel {
        HistoryViewModel(
            sessionHistoryRepository = get(),
            lyteNavigator = get(),
            // Clock.System — единственная точка «настоящего времени» модуля (см. CLAUDE.md).
            clock = Clock.System,
        )
    }
    viewModel { (sessionId: String) ->
        HistorySessionDetailsViewModel(
            sessionId = sessionId,
            sessionHistoryRepository = get(),
            lyteNavigator = get(),
        )
    }
}
