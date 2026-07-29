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
               COUNT(workout_exercise.id) AS exerciseCount
        FROM workout
        LEFT JOIN workout_exercise ON workout_exercise.workout_id = workout.id
        WHERE workout.is_archived = 0
        GROUP BY workout.id
        """,
    )
    abstract fun observeItems(): Flow<List<WorkoutItemWithExerciseCount>>

    @Transaction
    @Query("SELECT * FROM workout WHERE id = :id LIMIT 1")
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

    @Query("UPDATE workout SET is_archived = 1 WHERE id = :id")
    abstract suspend fun archiveWorkout(id: String)

    /**
     * Удаляет программу, если на неё не ссылается ни одна сессия трекера; иначе — архивирует
     * (soft delete), чтобы история сохранила ссылку на программу. Всё одной транзакцией:
     * подсчёт и запись атомарны.
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
     */
    @Transaction
    open suspend fun saveWorkoutGraph(
        workout: WorkoutDatabaseEntity,
        exercises: List<ExerciseDatabaseEntity>,
        crossRefs: List<WorkoutExerciseCrossRefDatabaseEntity>,
        sets: List<WorkoutSetDatabaseEntity>,
    ) {
        deleteCrossRefsByWorkout(workout.id)
        upsertWorkout(workout)
        upsertExercises(exercises)
        insertCrossRefs(crossRefs)
        insertSets(sets)
    }

    /**
     * Обновляет цели подходов программы одной транзакцией. Структуру программы (состав и порядок
     * упражнений, число подходов) и флаги `is_archived` не трогает — в отличие от [saveWorkoutGraph],
     * который пересоздаёт связки и апсертит строки `workout`/`exercise`.
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
