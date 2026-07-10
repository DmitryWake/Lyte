package com.nikolaevskii.lyte.feature.workout.data.repository

import com.nikolaevskii.lyte.core.db.workout.ExerciseDao
import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity

internal class FakeExerciseDao : ExerciseDao {

    private val exercises = mutableMapOf<String, ExerciseDatabaseEntity>()

    /**
     * Повторяет контракт SQL-запроса: подстрока ищется по уже нормализованному названию, результат
     * отсортирован по нему же. Экранирование `LIKE` здесь не воспроизводится — за него отвечает
     * вызывающая сторона, и в фейке спецсимволы остаются обычными символами, как и в SQLite с `ESCAPE`.
     */
    override suspend fun search(normalizedQuery: String): List<ExerciseDatabaseEntity> =
        exercises.values
            .filter { exercise -> exercise.nameNormalized.contains(normalizedQuery.unescapedFromLike()) }
            .sortedBy { exercise -> exercise.nameNormalized }

    override suspend fun getById(id: String): ExerciseDatabaseEntity? = exercises[id]

    override suspend fun upsert(exercise: ExerciseDatabaseEntity) {
        exercises[exercise.id] = exercise
    }

    override suspend fun deleteById(id: String) {
        exercises.remove(id)
    }

    private fun String.unescapedFromLike(): String = replace("\\%", "%").replace("\\_", "_").replace("\\\\", "\\")
}
