package com.nikolaevskii.lyte.feature.workout.di

import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.ExerciseCreatorViewModel
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.ExercisePickerViewModel
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutDetailsViewModel
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// Репозитории (WorkoutRepository/WorkoutExerciseRepository) регистрирует coreWorkoutModule().
val featureWorkoutModule = module {
    viewModelOf(::WorkoutListViewModel)
    viewModel { (initialId: String?) ->
        WorkoutDetailsViewModel(
            initialId = initialId,
            workoutRepository = get(),
            lyteNavigator = get(),
        )
    }
    viewModel { (initialQuery: String) ->
        ExercisePickerViewModel(initialQuery = initialQuery, workoutExerciseRepository = get())
    }
    viewModel { (initialName: String) ->
        ExerciseCreatorViewModel(initialName = initialName, workoutExerciseRepository = get())
    }
}
