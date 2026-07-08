package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Embedded
import androidx.room.Relation

/** Полный граф тренировки: сама тренировка со списком упражнений и их подходов. */
data class WorkoutWithExercises(
    @Embedded
    val workout: WorkoutDatabaseEntity,
    @Relation(
        entity = WorkoutExerciseCrossRefDatabaseEntity::class,
        parentColumn = "id",
        entityColumn = "workout_id",
    )
    val exercises: List<WorkoutExerciseWithSets>,
)
