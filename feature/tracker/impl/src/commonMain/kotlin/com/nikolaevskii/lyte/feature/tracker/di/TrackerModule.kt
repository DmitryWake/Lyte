package com.nikolaevskii.lyte.feature.tracker.di

import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.TrackerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureTrackerModule = module {
    viewModelOf(::TrackerViewModel)
}
