package com.nikolaevskii.lyte.core.session.domain.model

import kotlin.time.Instant

/**
 * Сессия тренировки — снапшот программы плюс фактический прогресс по подходам.
 * [finishedAt] `null` — сессия активна. [currentExerciseId] — id вручную выбранного упражнения
 * (`null` — не выбирали); сам объект берётся из [exercises] по этому id, чтобы не дублировать состояние.
 */
data class WorkoutSessionEntity(
    val id: String,
    val program: SessionProgramEntity,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val currentExerciseId: String?,
    val exercises: List<SessionExerciseEntity>,
)
