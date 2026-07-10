package com.nikolaevskii.lyte.feature.workout.data.repository

import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutDao
import com.nikolaevskii.lyte.core.db.workout.WorkoutDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutExerciseCrossRefDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutExerciseWithSets
import com.nikolaevskii.lyte.core.db.workout.WorkoutItemWithExerciseCount
import com.nikolaevskii.lyte.core.db.workout.WorkoutSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutWithExercises

/**
 * In-memory реализация [WorkoutDao] для тестов репозитория. `saveWorkoutGraph`
 * наследует транзакционное тело базового класса — проверяется и оркестрация.
 * Каскад (`ON DELETE CASCADE`) имитируется вручную в методах удаления.
 */
internal class FakeWorkoutDao : WorkoutDao() {

    /** Число сессий трекера, ссылающихся на программу — задаётся тестом, чтобы проверить архивацию. */
    val sessionCountByWorkout = mutableMapOf<String, Int>()

    private val workouts = mutableMapOf<String, WorkoutDatabaseEntity>()
    private val exercises = mutableMapOf<String, ExerciseDatabaseEntity>()
    private val crossRefs = mutableListOf<WorkoutExerciseCrossRefDatabaseEntity>()
    private val sets = mutableListOf<WorkoutSetDatabaseEntity>()

    override suspend fun getItems(): List<WorkoutItemWithExerciseCount> =
        workouts.values
            .filterNot { it.isArchived }
            .map { workout ->
                WorkoutItemWithExerciseCount(
                    id = workout.id,
                    name = workout.name,
                    description = workout.description,
                    exerciseCount = crossRefs.count { it.workoutId == workout.id },
                )
            }

    override suspend fun getWithExercises(id: String): WorkoutWithExercises? {
        val workout = workouts[id] ?: return null
        // Дети возвращаются в обратном порядке вставки — репозиторий обязан
        // восстановить порядок по position.
        val exerciseRows = crossRefs
            .filter { it.workoutId == id }
            .reversed()
            .map { crossRef ->
                WorkoutExerciseWithSets(
                    crossRef = crossRef,
                    exercise = exercises.getValue(crossRef.exerciseId),
                    sets = sets
                        .filter { it.workoutExerciseId == crossRef.id }
                        .reversed(),
                )
            }
        return WorkoutWithExercises(workout = workout, exercises = exerciseRows)
    }

    override suspend fun upsertWorkout(workout: WorkoutDatabaseEntity) {
        workouts[workout.id] = workout
    }

    override suspend fun upsertExercises(exercises: List<ExerciseDatabaseEntity>) {
        exercises.forEach { exercise -> this.exercises[exercise.id] = exercise }
    }

    override suspend fun insertCrossRefs(rows: List<WorkoutExerciseCrossRefDatabaseEntity>) {
        crossRefs += rows
    }

    override suspend fun insertSets(rows: List<WorkoutSetDatabaseEntity>) {
        sets += rows
    }

    override suspend fun deleteCrossRefsByWorkout(id: String) {
        val removed = crossRefs.filter { it.workoutId == id }
        crossRefs.removeAll(removed)
        val removedIds = removed.map { it.id }.toSet()
        sets.removeAll { it.workoutExerciseId in removedIds }
    }

    override suspend fun deleteWorkout(id: String) {
        workouts.remove(id)
        deleteCrossRefsByWorkout(id)
    }

    override suspend fun countSessionsForWorkout(id: String): Int =
        sessionCountByWorkout[id] ?: 0

    override suspend fun archiveWorkout(id: String) {
        workouts[id]?.let { workouts[id] = it.copy(isArchived = true) }
    }
}
