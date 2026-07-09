package com.nikolaevskii.lyte.feature.workout.di

import com.nikolaevskii.lyte.feature.workout.data.repository.WorkoutExerciseRepositoryImpl
import com.nikolaevskii.lyte.feature.workout.data.repository.WorkoutRepositoryImpl
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutExerciseRepository
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutDetailsViewModel
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureWorkoutModule = module {
    single<WorkoutRepository> { WorkoutRepositoryImpl(workoutDao = get()) }
    single<WorkoutExerciseRepository> { WorkoutExerciseRepositoryImpl(exerciseDao = get()) }

    viewModelOf(::WorkoutListViewModel)
    viewModel { (initialId: String?) ->
        WorkoutDetailsViewModel(
            initialId = initialId,
            workoutRepository = get(),
            lyteNavigator = get(),
        )
    }
}
