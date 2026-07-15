package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal class FakeWorkoutRepository(
    initialItems: List<WorkoutItemEntity> = emptyList(),
) : WorkoutRepository {

    // Реактивный источник: observeWorkouts переэмитит при изменении items (create/delete), как настоящий Room.
    private val itemsFlow = MutableStateFlow(initialItems)
    var items: List<WorkoutItemEntity>
        get() = itemsFlow.value
        set(value) {
            itemsFlow.value = value
        }
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

    override fun observeWorkouts(): Flow<List<WorkoutItemEntity>> = flow {
        getWorkoutsError?.let { throw it }
        emitAll(itemsFlow)
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
