package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Embedded
import androidx.room.Relation

/** Связка «тренировка ↔ упражнение» вместе с самим упражнением и его подходами. */
data class WorkoutExerciseWithSets(
    @Embedded
    val crossRef: WorkoutExerciseCrossRefDatabaseEntity,
    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "id",
    )
    val exercise: ExerciseDatabaseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workout_exercise_id",
    )
    val sets: List<WorkoutSetDatabaseEntity>,
)
