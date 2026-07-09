package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository

internal class FakeWorkoutRepository(
    initialItems: List<WorkoutItemEntity> = emptyList(),
) : WorkoutRepository {

    var items: List<WorkoutItemEntity> = initialItems
    var getWorkoutsError: Throwable? = null
    val deletedIds = mutableListOf<String>()

    override suspend fun getWorkouts(): List<WorkoutItemEntity> {
        getWorkoutsError?.let { throw it }
        return items
    }

    override suspend fun getWorkout(id: String): WorkoutEntity? = null

    override suspend fun createWorkout(workoutEntity: WorkoutEntity) = Unit

    override suspend fun editWorkout(workoutEntity: WorkoutEntity) = Unit

    override suspend fun deleteWorkout(id: String) {
        deletedIds += id
        items = items.filterNot { it.id == id }
    }
}
