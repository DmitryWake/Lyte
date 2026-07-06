package com.nikolaevskii.lyte.di

import com.nikolaevskii.lyte.core.db.di.coreDbModule
import com.nikolaevskii.lyte.core.di.initKoin
import com.nikolaevskii.lyte.core.navigation.di.coreNavigationModule
import com.nikolaevskii.lyte.feature.history.di.featureHistoryModule
import com.nikolaevskii.lyte.feature.tracker.di.featureTrackerModule
import com.nikolaevskii.lyte.feature.workout.di.featureWorkoutModule
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration

fun initKoinShared(
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication =
    initKoin {
        appDeclaration()
        modules(
            coreDbModule(),
            coreNavigationModule,
            featureTrackerModule,
            featureWorkoutModule,
            featureHistoryModule,
        )
    }
