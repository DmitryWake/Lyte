package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FakeWorkoutRepository(
    initialItems: List<WorkoutItemEntity> = emptyList(),
    initialWorkout: WorkoutEntity? = null,
) : WorkoutRepository {

    var items: List<WorkoutItemEntity> = initialItems
    var getWorkoutsError: Throwable? = null
    var workout: WorkoutEntity? = initialWorkout
    var getWorkoutError: Throwable? = null

    override suspend fun getWorkouts(): List<WorkoutItemEntity> {
        getWorkoutsError?.let { throw it }
        return items
    }

    override fun observeWorkouts(): Flow<List<WorkoutItemEntity>> = flow { emit(getWorkouts()) }

    override suspend fun getWorkout(id: String): WorkoutEntity? {
        getWorkoutError?.let { throw it }
        return workout
    }

    override suspend fun createWorkout(workoutEntity: WorkoutEntity) = Unit

    override suspend fun editWorkout(workoutEntity: WorkoutEntity) = Unit

    override suspend fun updateWorkoutTargets(workoutEntity: WorkoutEntity) = Unit

    override suspend fun deleteWorkout(id: String) = Unit
}
