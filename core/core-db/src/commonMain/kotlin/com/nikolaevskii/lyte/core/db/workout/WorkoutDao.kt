package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao {

    @Query(
        """
        SELECT workout.id AS id, workout.name AS name, workout.description AS description,
               workout.accent AS accent, workout.glyph AS glyph,
               COUNT(workout_exercise.id) AS exerciseCount
        FROM workout
        LEFT JOIN workout_exercise ON workout_exercise.workout_id = workout.id
        WHERE workout.is_archived = 0
        GROUP BY workout.id
        """,
    )
    abstract suspend fun getItems(): List<WorkoutItemWithExerciseCount>

    /** Реактивная версия [getItems]: эмитит при любом изменении задетых таблиц. */
    @Query(
        """
        SELECT workout.id AS id, workout.name AS name, workout.description AS description,
               workout.accent AS accent, workout.glyph AS glyph,
               COUNT(workout_exercise.id) AS exerciseCount
        FROM workout
        LEFT JOIN workout_exercise ON workout_exercise.workout_id = workout.id
        WHERE workout.is_archived = 0
        GROUP BY workout.id
        """,
    )
    abstract fun observeItems(): Flow<List<WorkoutItemWithExerciseCount>>

    /**
     * Программа со всем составом. Архивную (`is_archived = 1`) не отдаёт: по id её не должен получать
     * никто — иначе с уже открытого превью стартует тренировка по удалённой программе, а редактор
     * возвращает её в списки первым же сохранением. История фильтром не задета: сессия хранит снапшот
     * программы и на `workout` не ссылается (см.
     * [com.nikolaevskii.lyte.core.db.session.WorkoutSessionDatabaseEntity]).
     *
     * Упражнения внутри графа, наоборот, не фильтруются: архивное упражнение остаётся частью
     * программы, и отсев молча проредил бы её состав — тот же довод, что у [ExerciseDao.getById].
     */
    @Transaction
    @Query("SELECT * FROM workout WHERE id = :id AND is_archived = 0 LIMIT 1")
    abstract suspend fun getWithExercises(id: String): WorkoutWithExercises?

    @Upsert
    abstract suspend fun upsertWorkout(workout: WorkoutDatabaseEntity)

    @Upsert
    abstract suspend fun upsertExercises(exercises: List<ExerciseDatabaseEntity>)

    @Insert
    abstract suspend fun insertCrossRefs(rows: List<WorkoutExerciseCrossRefDatabaseEntity>)

    @Insert
    abstract suspend fun insertSets(rows: List<WorkoutSetDatabaseEntity>)

    @Query("DELETE FROM workout_exercise WHERE workout_id = :id")
    abstract suspend fun deleteCrossRefsByWorkout(id: String)

    /**
     * Меняет цель одного подхода, адресуя его позициями. Если упражнения или подхода с такими
     * позициями в программе нет (её отредактировали), обновляется ноль строк — это не ошибка.
     */
    @Query(
        """
        UPDATE workout_set SET count = :count, weight = :weight
        WHERE position = :setPosition
          AND workout_exercise_id IN (
              SELECT id FROM workout_exercise WHERE workout_id = :workoutId AND position = :exercisePosition
          )
        """,
    )
    abstract suspend fun updateSetTarget(
        workoutId: String,
        exercisePosition: Int,
        setPosition: Int,
        count: Int,
        weight: Double?,
    )

    @Query("DELETE FROM workout WHERE id = :id")
    abstract suspend fun deleteWorkout(id: String)

    @Query("SELECT COUNT(*) FROM workout_session WHERE program_id = :id")
    abstract suspend fun countSessionsForWorkout(id: String): Int

    /** Архивна ли программа. `EXISTS`, а не выборка колонки: у несуществующей строки ответ `false`. */
    @Query("SELECT EXISTS(SELECT 1 FROM workout WHERE id = :id AND is_archived = 1)")
    abstract suspend fun isWorkoutArchived(id: String): Boolean

    /** Те из [ids], что уже заархивированы. Пустой список — пустой ответ (`IN ()` в SQLite легален). */
    @Query("SELECT id FROM exercise WHERE id IN (:ids) AND is_archived = 1")
    abstract suspend fun getArchivedExerciseIds(ids: List<String>): List<String>

    @Query("UPDATE workout SET is_archived = 1 WHERE id = :id")
    abstract suspend fun archiveWorkout(id: String)

    /**
     * Удаляет программу, если на неё не ссылается ни одна сессия трекера; иначе — архивирует
     * (soft delete): строка остаётся в БД, но становится невидимой и для списков, и для чтения по id
     * ([getWithExercises]). Всё одной транзакцией: подсчёт и запись атомарны.
     */
    @Transaction
    open suspend fun deleteOrArchiveWorkout(id: String) {
        if (countSessionsForWorkout(id) > 0) {
            archiveWorkout(id)
        } else {
            deleteWorkout(id)
        }
    }

    /**
     * Сохраняет граф тренировки одной транзакцией. Покрывает и создание (детей нет),
     * и редактирование (старые связки и их подходы удаляются каскадом перед вставкой новых).
     *
     * Для [workout]/[exercises] используется `@Upsert`, а не `@Insert(REPLACE)`:
     * `INSERT OR REPLACE` удалил бы конфликтную строку и через `ON DELETE CASCADE` снёс детей.
     *
     * `is_archived` — собственность БД: доменная модель о нём не знает и приносит `false` в каждой
     * строке, поэтому апсерт «как принесли» снял бы архив и с программы, и с её упражнений, вернув
     * удалённое в списки. Флаг переносится из текущих строк — весь пересчёт в одной транзакции с
     * записью. Точечный [updateSetTargets] обходит ровно эту проблему с другой стороны: он вообще
     * не трогает строки `workout`/`exercise`.
     */
    @Transaction
    open suspend fun saveWorkoutGraph(
        workout: WorkoutDatabaseEntity,
        exercises: List<ExerciseDatabaseEntity>,
        crossRefs: List<WorkoutExerciseCrossRefDatabaseEntity>,
        sets: List<WorkoutSetDatabaseEntity>,
    ) {
        val isWorkoutArchived = isWorkoutArchived(workout.id)
        val archivedExerciseIds = getArchivedExerciseIds(exercises.map { exercise -> exercise.id }).toSet()

        deleteCrossRefsByWorkout(workout.id)
        upsertWorkout(workout.copy(isArchived = isWorkoutArchived))
        upsertExercises(
            exercises.map { exercise -> exercise.copy(isArchived = exercise.id in archivedExerciseIds) },
        )
        insertCrossRefs(crossRefs)
        insertSets(sets)
    }

    /**
     * Обновляет цели подходов программы одной транзакцией. Структуру программы (состав и порядок
     * упражнений, число подходов) и строки `workout`/`exercise` не трогает вовсе — в отличие от
     * [saveWorkoutGraph], который пересоздаёт связки и апсертит обе эти таблицы.
     */
    @Transaction
    open suspend fun updateSetTargets(workoutId: String, targets: List<WorkoutSetTargetUpdate>) {
        targets.forEach { target ->
            updateSetTarget(
                workoutId = workoutId,
                exercisePosition = target.exercisePosition,
                setPosition = target.setPosition,
                count = target.count,
                weight = target.weight,
            )
        }
    }
}
