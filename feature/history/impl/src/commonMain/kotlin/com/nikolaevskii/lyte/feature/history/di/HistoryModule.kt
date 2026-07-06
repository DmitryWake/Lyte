package com.nikolaevskii.lyte.feature.history.di

import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHistoryModule = module {
    viewModelOf(::HistoryViewModel)
}
