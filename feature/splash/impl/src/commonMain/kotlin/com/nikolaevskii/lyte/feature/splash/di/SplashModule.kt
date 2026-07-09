package com.nikolaevskii.lyte.feature.splash.di

import com.nikolaevskii.lyte.feature.splash.data.initializer.ExerciseLibraryInitializer
import com.nikolaevskii.lyte.feature.splash.data.repository.AppLaunchStateRepositoryImpl
import com.nikolaevskii.lyte.feature.splash.domain.initializer.AppInitializationManager
import com.nikolaevskii.lyte.feature.splash.domain.initializer.AppInitializer
import com.nikolaevskii.lyte.feature.splash.domain.repository.AppLaunchStateRepository
import com.nikolaevskii.lyte.feature.splash.presentation.viewmodel.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSplashModule = module {
    single<AppLaunchStateRepository> { AppLaunchStateRepositoryImpl(appLaunchStateDao = get()) }
    single<AppInitializer> {
        ExerciseLibraryInitializer(exerciseRepository = get(), appLaunchStateRepository = get())
    }
    single { AppInitializationManager(initializers = getAll<AppInitializer>()) }

    viewModelOf(::SplashViewModel)
}
