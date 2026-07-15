package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository

internal class FakeWorkoutRepository(
    initialItems: List<WorkoutItemEntity> = emptyList(),
) : WorkoutRepository {

    var items: List<WorkoutItemEntity> = initialItems
    var getWorkoutsError: Throwable? = null
    var workoutToReturn: WorkoutEntity? = null
    var getWorkoutError: Throwable? = null
    var createWorkoutError: Throwable? = null
    var editWorkoutError: Throwable? = null
    val deletedIds = mutableListOf<String>()
    val createdWorkouts = mutableListOf<WorkoutEntity>()
    val editedWorkouts = mutableListOf<WorkoutEntity>()
    val getWorkoutCalls = mutableListOf<String>()

    override suspend fun getWorkouts(): List<WorkoutItemEntity> {
        getWorkoutsError?.let { throw it }
        return items
    }

    override suspend fun getWorkout(id: String): WorkoutEntity? {
        getWorkoutCalls += id
        getWorkoutError?.let { throw it }
        return workoutToReturn
    }

    override suspend fun createWorkout(workoutEntity: WorkoutEntity) {
        createWorkoutError?.let { throw it }
        createdWorkouts += workoutEntity
    }

    override suspend fun editWorkout(workoutEntity: WorkoutEntity) {
        editWorkoutError?.let { throw it }
        editedWorkouts += workoutEntity
    }

    override suspend fun deleteWorkout(id: String) {
        deletedIds += id
        items = items.filterNot { it.id == id }
    }
}
