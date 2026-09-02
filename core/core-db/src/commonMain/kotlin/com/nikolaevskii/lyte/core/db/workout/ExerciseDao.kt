package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
abstract class ExerciseDao {

    /**
     * Библиотека упражнений, отфильтрованная по подстроке в названии и отсортированная по нему же,
     * по возрастанию. Архивные упражнения (`is_archived = 1`) в библиотеку не попадают. Пустой
     * [normalizedQuery] отдаёт всю неархивную библиотеку: `LIKE '%%'` совпадает со всем.
     *
     * [normalizedQuery] — подстрока, уже приведённая **тем же** правилом, что и колонка: нижний
     * регистр плюс обычный пробел вместо неразрывного (см. [ExerciseDatabaseEntity.nameNormalized]).
     * Приводить регистр в SQL нельзя — `lower()` в SQLite работает только с ASCII.
     * Спецсимволы `LIKE` (`%`, `_`, `\`) вызывающая сторона обязана экранировать — отсюда `ESCAPE`.
     */
    @Query(
        "SELECT * FROM exercise " +
            "WHERE is_archived = 0 AND name_normalized LIKE '%' || :normalizedQuery || '%' ESCAPE '\\' " +
            "ORDER BY name_normalized",
    )
    abstract suspend fun search(normalizedQuery: String): List<ExerciseDatabaseEntity>

    // Без фильтра по is_archived: разрешает резолвить упражнение по id даже после архивации
    // (нужно программам и сессиям, которые на него ссылаются).
    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    abstract suspend fun getById(id: String): ExerciseDatabaseEntity?

    @Upsert
    abstract suspend fun upsert(exercise: ExerciseDatabaseEntity)

    @Query(
        """
        SELECT (SELECT COUNT(*) FROM workout_exercise WHERE exercise_id = :id)
             + (SELECT COUNT(*) FROM session_exercise WHERE exercise_id = :id)
        """,
    )
    abstract suspend fun countReferences(id: String): Int

    @Query("UPDATE exercise SET is_archived = 1 WHERE id = :id")
    abstract suspend fun archiveExercise(id: String)

    @Query("DELETE FROM exercise WHERE id = :id")
    abstract suspend fun deleteById(id: String)

    /**
     * Удаляет упражнение, если на него не ссылается ни одна программа или сессия; иначе — архивирует
     * (soft delete), чтобы существующие ссылки (и отображение имени в истории) остались валидными.
     * Всё одной транзакцией: подсчёт и запись атомарны.
     */
    @Transaction
    open suspend fun deleteOrArchiveExercise(id: String) {
        if (countReferences(id) > 0) {
            archiveExercise(id)
        } else {
            deleteById(id)
        }
    }
}
