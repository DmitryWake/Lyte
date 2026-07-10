package com.nikolaevskii.lyte.feature.workout.data.repository

import com.nikolaevskii.lyte.core.db.workout.ExerciseDao
import com.nikolaevskii.lyte.feature.workout.data.mapper.toDomainEntity
import com.nikolaevskii.lyte.feature.workout.data.mapper.toDatabaseEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutExerciseRepository

internal class WorkoutExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao,
) : WorkoutExerciseRepository {

    /**
     * Фильтрация и сортировка — в SQL (см. [ExerciseDao.search]), сюда остаётся только привести
     * запрос к тому же виду, в каком лежит `name_normalized`, и экранировать спецсимволы `LIKE`.
     */
    override suspend fun getExercises(query: String): List<WorkoutExerciseEntity> =
        exerciseDao.search(normalizedQuery = query.trim().lowercase().escapedForLike())
            .map { exercise -> exercise.toDomainEntity() }

    override suspend fun getExercise(id: String): WorkoutExerciseEntity? =
        exerciseDao.getById(id)?.toDomainEntity()

    override suspend fun createExercise(workoutExerciseEntity: WorkoutExerciseEntity) {
        exerciseDao.upsert(workoutExerciseEntity.toDatabaseEntity())
    }

    override suspend fun editExercises(workoutExerciseEntity: WorkoutExerciseEntity) {
        exerciseDao.upsert(workoutExerciseEntity.toDatabaseEntity())
    }

    override suspend fun deleteExercise(id: String) {
        exerciseDao.deleteById(id)
    }

    /** Без экранирования введённые пользователем `%` и `_` работали бы как маски `LIKE`. */
    private fun String.escapedForLike(): String =
        replace(LIKE_ESCAPE_CHAR, "$LIKE_ESCAPE_CHAR$LIKE_ESCAPE_CHAR")
            .replace("%", "$LIKE_ESCAPE_CHAR%")
            .replace("_", "${LIKE_ESCAPE_CHAR}_")

    private companion object {
        const val LIKE_ESCAPE_CHAR = "\\"
    }
}
