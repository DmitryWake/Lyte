package com.nikolaevskii.lyte.core.workout.di

import com.nikolaevskii.lyte.core.workout.data.repository.WorkoutExerciseRepositoryImpl
import com.nikolaevskii.lyte.core.workout.data.repository.WorkoutRepositoryImpl
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutExerciseRepository
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/** DAO приходят из `coreDbModule()`. */
fun coreWorkoutModule(): Module = module {
    single<WorkoutRepository> { WorkoutRepositoryImpl(workoutDao = get()) }
    single<WorkoutExerciseRepository> { WorkoutExerciseRepositoryImpl(exerciseDao = get()) }
}
