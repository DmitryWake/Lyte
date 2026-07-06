package com.nikolaevskii.lyte.feature.workout.di

import com.nikolaevskii.lyte.feature.workout.data.WorkoutRepositoryImpl
import com.nikolaevskii.lyte.feature.workout.domain.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutDetailsViewModel
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureWorkoutModule = module {
    single<WorkoutRepository> { WorkoutRepositoryImpl(dao = get()) }

    viewModelOf(::WorkoutListViewModel)
    viewModel { (initialId: Long) ->
        WorkoutDetailsViewModel(
            initialId = initialId,
            repository = get(),
            lyteNavigator = get(),
        )
    }
}
