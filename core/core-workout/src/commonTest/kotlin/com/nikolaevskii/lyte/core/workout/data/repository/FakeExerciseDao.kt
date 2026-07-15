package com.nikolaevskii.lyte.core.workout.data.repository

import com.nikolaevskii.lyte.core.db.workout.ExerciseDao
import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity

/**
 * In-memory реализация [ExerciseDao] для тестов репозитория. `deleteOrArchiveExercise` наследует
 * транзакционное тело базового класса — проверяется и решение «удалить vs архивировать».
 */
internal class FakeExerciseDao : ExerciseDao() {

    /** Число ссылок (программы + сессии) на упражнение — задаётся тестом, чтобы проверить архивацию. */
    val referenceCountByExercise = mutableMapOf<String, Int>()

    private val exercises = mutableMapOf<String, ExerciseDatabaseEntity>()

    /**
     * Повторяет контракт SQL-запроса: архивные упражнения скрыты, подстрока ищется по уже
     * нормализованному названию, результат отсортирован по нему же. Экранирование `LIKE` здесь не
     * воспроизводится — за него отвечает вызывающая сторона, спецсимволы остаются обычными, как с `ESCAPE`.
     */
    override suspend fun search(normalizedQuery: String): List<ExerciseDatabaseEntity> =
        exercises.values
            .filterNot { exercise -> exercise.isArchived }
            .filter { exercise -> exercise.nameNormalized.contains(normalizedQuery.unescapedFromLike()) }
            .sortedBy { exercise -> exercise.nameNormalized }

    override suspend fun getById(id: String): ExerciseDatabaseEntity? = exercises[id]

    override suspend fun upsert(exercise: ExerciseDatabaseEntity) {
        exercises[exercise.id] = exercise
    }

    override suspend fun countReferences(id: String): Int = referenceCountByExercise[id] ?: 0

    override suspend fun archiveExercise(id: String) {
        exercises[id]?.let { exercises[id] = it.copy(isArchived = true) }
    }

    override suspend fun deleteById(id: String) {
        exercises.remove(id)
    }

    private fun String.unescapedFromLike(): String = replace("\\%", "%").replace("\\_", "_").replace("\\\\", "\\")
}
