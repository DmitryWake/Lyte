package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
abstract class WorkoutDao {

    @Query(
        """
        SELECT workout.id AS id, workout.name AS name, workout.description AS description,
               COUNT(workout_exercise.id) AS exerciseCount
        FROM workout
        LEFT JOIN workout_exercise ON workout_exercise.workout_id = workout.id
        GROUP BY workout.id
        """,
    )
    abstract suspend fun getItems(): List<WorkoutItemWithExerciseCount>

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

    @Query("DELETE FROM workout WHERE id = :id")
    abstract suspend fun deleteWorkout(id: String)

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
}
