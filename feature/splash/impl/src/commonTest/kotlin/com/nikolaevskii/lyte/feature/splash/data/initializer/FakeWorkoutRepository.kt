package com.nikolaevskii.lyte.feature.splash.data.initializer

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository

internal class FakeWorkoutRepository : WorkoutRepository {

    private val workouts = mutableMapOf<String, WorkoutEntity>()

    override suspend fun getWorkouts(): List<WorkoutItemEntity> =
        workouts.values.map { workout ->
            WorkoutItemEntity(
                id = workout.id,
                name = workout.name,
                description = workout.description,
                exerciseCount = workout.exercises.size,
            )
        }

    override suspend fun getWorkout(id: String): WorkoutEntity? = workouts[id]

    override suspend fun createWorkout(workoutEntity: WorkoutEntity) {
        workouts[workoutEntity.id] = workoutEntity
    }

    override suspend fun editWorkout(workoutEntity: WorkoutEntity) {
        workouts[workoutEntity.id] = workoutEntity
    }

    override suspend fun deleteWorkout(id: String) {
        workouts.remove(id)
    }
}
