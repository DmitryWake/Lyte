package com.nikolaevskii.lyte.core.workout.data.model

import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutExerciseCrossRefDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutSetDatabaseEntity

/** Плоское представление доменного графа тренировки для записи в БД. */
internal data class WorkoutRowsModel(
    val workout: WorkoutDatabaseEntity,
    val exercises: List<ExerciseDatabaseEntity>,
    val crossRefs: List<WorkoutExerciseCrossRefDatabaseEntity>,
    val sets: List<WorkoutSetDatabaseEntity>,
)