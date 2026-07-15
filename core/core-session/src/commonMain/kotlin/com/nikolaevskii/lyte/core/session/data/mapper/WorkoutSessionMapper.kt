package com.nikolaevskii.lyte.core.session.data.mapper

import com.nikolaevskii.lyte.core.db.session.SessionExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionExerciseWithSets
import com.nikolaevskii.lyte.core.db.session.SessionItemWithSetCounts
import com.nikolaevskii.lyte.core.db.session.SessionSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionWithExercises
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDatabaseEntity
import com.nikolaevskii.lyte.core.session.data.model.SessionRowsModel
import com.nikolaevskii.lyte.core.session.domain.model.SessionExerciseEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionProgramEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import kotlin.time.Instant

/** Разделитель сегментов синтетических id упражнений/подходов сессии. */
private const val ID_SEGMENT_SEPARATOR: String = "#"

/**
 * Строит граф строк сессии — снапшот программы: имена и цели копируются, факты пустые
 * ([SessionSetDatabaseEntity.resultStatus] `= null`), заметки пустые.
 */
internal fun WorkoutEntity.toSessionRows(sessionId: String, startedAt: Instant): SessionRowsModel {
    val sessionRow = WorkoutSessionDatabaseEntity(
        id = sessionId,
        programId = id,
        programName = name,
        startedAt = startedAt.toEpochMilliseconds(),
        finishedAt = null,
        currentExerciseId = null,
    )
    val exerciseRows = mutableListOf<SessionExerciseDatabaseEntity>()
    val setRows = mutableListOf<SessionSetDatabaseEntity>()

    exercises.forEachIndexed { exerciseIndex, exerciseWithReps ->
        val sessionExerciseId = "$sessionId$ID_SEGMENT_SEPARATOR$exerciseIndex"

        // Имя/описание не копируются: session_exercise ссылается на живое упражнение по exercise_id.
        exerciseRows += SessionExerciseDatabaseEntity(
            id = sessionExerciseId,
            sessionId = sessionId,
            exerciseId = exerciseWithReps.exercise.id,
            position = exerciseIndex,
        )
        exerciseWithReps.reps.forEachIndexed { repIndex, rep ->
            setRows += SessionSetDatabaseEntity(
                id = "$sessionExerciseId$ID_SEGMENT_SEPARATOR$repIndex",
                sessionExerciseId = sessionExerciseId,
                position = repIndex,
                targetCount = rep.count,
                targetWeight = rep.weight,
                resultStatus = null,
                resultCount = null,
                resultWeight = null,
                note = "",
            )
        }
    }

    return SessionRowsModel(
        session = sessionRow,
        exercises = exerciseRows,
        sets = setRows,
    )
}

internal fun SessionWithExercises.toDomainEntity(): WorkoutSessionEntity =
    WorkoutSessionEntity(
        id = session.id,
        program = SessionProgramEntity(id = session.programId, name = session.programName),
        startedAt = Instant.fromEpochMilliseconds(session.startedAt),
        finishedAt = session.finishedAt?.let(Instant::fromEpochMilliseconds),
        currentExerciseId = session.currentExerciseId,
        exercises = exercises
            .sortedBy { it.sessionExercise.position }
            .map { it.toDomainEntity() },
    )

internal fun SessionItemWithSetCounts.toItemEntity(): WorkoutSessionItemEntity =
    WorkoutSessionItemEntity(
        id = id,
        program = SessionProgramEntity(id = programId, name = programName),
        startedAt = Instant.fromEpochMilliseconds(startedAt),
        finishedAt = Instant.fromEpochMilliseconds(finishedAt),
        completedSetCount = completedSetCount,
        totalSetCount = totalSetCount,
    )

private fun SessionExerciseWithSets.toDomainEntity(): SessionExerciseEntity =
    SessionExerciseEntity(
        id = sessionExercise.id,
        exercise = WorkoutExerciseEntity(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
        ),
        sets = sets
            .sortedBy { it.position }
            .map { it.toDomainEntity() },
    )

private fun SessionSetDatabaseEntity.toDomainEntity(): SessionSetEntity =
    SessionSetEntity(
        id = id,
        target = SessionSetValueEntity(count = targetCount, weight = targetWeight),
        result = toResultEntity(),
        note = note,
    )

private fun SessionSetDatabaseEntity.toResultEntity(): SessionSetResultEntity? =
    when (resultStatus) {
        null -> null
        SessionSetDatabaseEntity.RESULT_STATUS_SKIPPED -> SessionSetResultEntity.Skipped
        SessionSetDatabaseEntity.RESULT_STATUS_COMPLETED -> SessionSetResultEntity.Completed(
            actual = SessionSetValueEntity(count = resultCount ?: 0, weight = resultWeight),
        )
        else -> null
    }
