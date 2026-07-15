package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutExerciseRepository
import kotlinx.coroutines.delay

internal class FakeWorkoutExerciseRepository(
    initialExercises: List<WorkoutExerciseEntity> = emptyList(),
) : WorkoutExerciseRepository {

    var exercises: List<WorkoutExerciseEntity> = initialExercises
    var getExercisesError: Throwable? = null
    var createExerciseError: Throwable? = null

    /** Задержка ответа: даёт тестам поймать запрос «в полёте» и отменить его более свежим. */
    var getExercisesDelayMillis: Long = 0
    val createdExercises = mutableListOf<WorkoutExerciseEntity>()
    val editedExercises = mutableListOf<WorkoutExerciseEntity>()
    val deletedIds = mutableListOf<String>()
    val queries = mutableListOf<String>()

    /** Повторяет контракт репозитория: фильтр по подстроке названия без учёта регистра, сортировка по названию. */
    override suspend fun getExercises(query: String): List<WorkoutExerciseEntity> {
        queries += query
        delay(getExercisesDelayMillis)
        getExercisesError?.let { throw it }
        val trimmedQuery = query.trim()
        return exercises
            .filter { exercise -> exercise.name.contains(trimmedQuery, ignoreCase = true) }
            .sortedBy { exercise -> exercise.name.lowercase() }
    }

    override suspend fun getExercise(id: String): WorkoutExerciseEntity? = exercises.firstOrNull { it.id == id }

    override suspend fun createExercise(workoutExerciseEntity: WorkoutExerciseEntity) {
        createExerciseError?.let { throw it }
        createdExercises += workoutExerciseEntity
        exercises = exercises + workoutExerciseEntity
    }

    override suspend fun editExercises(workoutExerciseEntity: WorkoutExerciseEntity) {
        editedExercises += workoutExerciseEntity
    }

    override suspend fun deleteExercise(id: String) {
        deletedIds += id
        exercises = exercises.filterNot { it.id == id }
    }
}
