package com.nikolaevskii.lyte.core.db.session

import androidx.room.Embedded
import androidx.room.Relation

/** Полный граф сессии: сама сессия со списком упражнений и их подходов. */
data class SessionWithExercises(
    @Embedded
    val session: WorkoutSessionDatabaseEntity,
    @Relation(
        entity = SessionExerciseDatabaseEntity::class,
        parentColumn = "id",
        entityColumn = "session_id",
    )
    val exercises: List<SessionExerciseWithSets>,
)
