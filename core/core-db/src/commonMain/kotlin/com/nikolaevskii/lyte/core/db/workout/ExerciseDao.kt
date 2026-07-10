package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ExerciseDao {

    /**
     * Библиотека упражнений, отфильтрованная по подстроке в названии и отсортированная по нему же,
     * по возрастанию. Пустой [normalizedQuery] отдаёт всю библиотеку: `LIKE '%%'` совпадает со всем.
     *
     * [normalizedQuery] — уже приведённая к нижнему регистру подстрока: приводить регистр в SQL
     * нельзя, `lower()` в SQLite работает только с ASCII (см. [ExerciseDatabaseEntity.nameNormalized]).
     * Спецсимволы `LIKE` (`%`, `_`, `\`) вызывающая сторона обязана экранировать — отсюда `ESCAPE`.
     */
    @Query(
        "SELECT * FROM exercise " +
            "WHERE name_normalized LIKE '%' || :normalizedQuery || '%' ESCAPE '\\' " +
            "ORDER BY name_normalized",
    )
    suspend fun search(normalizedQuery: String): List<ExerciseDatabaseEntity>

    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExerciseDatabaseEntity?

    @Upsert
    suspend fun upsert(exercise: ExerciseDatabaseEntity)

    @Query("DELETE FROM exercise WHERE id = :id")
    suspend fun deleteById(id: String)
}
