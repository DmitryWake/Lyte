package com.nikolaevskii.lyte.core.session.data.model

import com.nikolaevskii.lyte.core.db.session.SessionExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDatabaseEntity

/** Плоское представление доменного графа сессии для записи в БД. */
internal data class SessionRowsModel(
    val session: WorkoutSessionDatabaseEntity,
    val exercises: List<SessionExerciseDatabaseEntity>,
    val sets: List<SessionSetDatabaseEntity>,
)
