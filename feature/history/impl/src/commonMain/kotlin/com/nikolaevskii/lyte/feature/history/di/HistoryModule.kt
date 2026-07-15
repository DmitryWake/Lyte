package com.nikolaevskii.lyte.feature.history.di

import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistorySessionDetailsViewModel
import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// SessionHistoryRepository (read-контракт) приходит из coreSessionModule().
val featureHistoryModule = module {
    viewModelOf(::HistoryViewModel)
    viewModel { (sessionId: String) ->
        HistorySessionDetailsViewModel(
            sessionId = sessionId,
            sessionHistoryRepository = get(),
            lyteNavigator = get(),
        )
    }
}
