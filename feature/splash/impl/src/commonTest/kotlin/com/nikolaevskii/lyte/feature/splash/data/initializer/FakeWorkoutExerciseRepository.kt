package com.nikolaevskii.lyte.feature.splash.data.initializer

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutExerciseRepository

internal class FakeWorkoutExerciseRepository : WorkoutExerciseRepository {

    private val exercises = mutableMapOf<String, WorkoutExerciseEntity>()

    override suspend fun getExercises(query: String): List<WorkoutExerciseEntity> =
        exercises.values.filter { exercise -> exercise.name.contains(query.trim(), ignoreCase = true) }

    override suspend fun getExercise(id: String): WorkoutExerciseEntity? = exercises[id]

    override suspend fun createExercise(workoutExerciseEntity: WorkoutExerciseEntity) {
        exercises[workoutExerciseEntity.id] = workoutExerciseEntity
    }

    override suspend fun editExercises(workoutExerciseEntity: WorkoutExerciseEntity) {
        exercises[workoutExerciseEntity.id] = workoutExerciseEntity
    }

    override suspend fun deleteExercise(id: String) {
        exercises.remove(id)
    }
}
