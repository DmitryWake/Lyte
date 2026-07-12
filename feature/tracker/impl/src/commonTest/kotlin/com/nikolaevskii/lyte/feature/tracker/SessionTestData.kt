package com.nikolaevskii.lyte.feature.tracker

import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionExerciseEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionProgramEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import kotlin.time.Instant

/** Краткие билдеры доменных моделей сессии для тестов маппера/прогрессии/VM. */

internal fun sessionSet(
    id: String,
    targetCount: Int,
    targetWeight: Double?,
    result: SessionSetResultEntity? = null,
    note: String = "",
): SessionSetEntity = SessionSetEntity(
    id = id,
    target = SessionSetValueEntity(count = targetCount, weight = targetWeight),
    result = result,
    note = note,
)

internal fun completed(count: Int, weight: Double?): SessionSetResultEntity.Completed =
    SessionSetResultEntity.Completed(SessionSetValueEntity(count = count, weight = weight))

internal fun sessionExercise(
    id: String,
    name: String,
    sets: List<SessionSetEntity>,
): SessionExerciseEntity = SessionExerciseEntity(
    id = id,
    exercise = WorkoutExerciseEntity(id = "lib-$id", name = name),
    sets = sets,
)

internal fun workoutSession(
    id: String = "session-1",
    programName: String = "Push Day",
    startedAtMillis: Long = 0,
    finishedAtMillis: Long? = null,
    currentExerciseId: String? = null,
    exercises: List<SessionExerciseEntity>,
): WorkoutSessionEntity = WorkoutSessionEntity(
    id = id,
    program = SessionProgramEntity(id = "prog-1", name = programName),
    startedAt = Instant.fromEpochMilliseconds(startedAtMillis),
    finishedAt = finishedAtMillis?.let { millis -> Instant.fromEpochMilliseconds(millis) },
    currentExerciseId = currentExerciseId,
    exercises = exercises,
)
