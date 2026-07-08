package com.nikolaevskii.lyte.feature.workout.data.repository

import com.nikolaevskii.lyte.core.db.workout.ExerciseDao
import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity

internal class FakeExerciseDao : ExerciseDao {

    private val exercises = mutableMapOf<String, ExerciseDatabaseEntity>()

    override suspend fun getAll(): List<ExerciseDatabaseEntity> = exercises.values.toList()

    override suspend fun getById(id: String): ExerciseDatabaseEntity? = exercises[id]

    override suspend fun upsert(exercise: ExerciseDatabaseEntity) {
        exercises[exercise.id] = exercise
    }

    override suspend fun deleteById(id: String) {
        exercises.remove(id)
    }
}
