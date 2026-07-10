package com.nikolaevskii.lyte.core.db.session

import androidx.room.Embedded
import androidx.room.Relation
import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity

/**
 * Упражнение сессии вместе с самим упражнением-библиотекой (живое имя/описание по `exercise_id`)
 * и его подходами; порядок под-списка потребитель восстанавливает по `position`.
 */
data class SessionExerciseWithSets(
    @Embedded
    val sessionExercise: SessionExerciseDatabaseEntity,
    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "id",
    )
    val exercise: ExerciseDatabaseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "session_exercise_id",
    )
    val sets: List<SessionSetDatabaseEntity>,
)
