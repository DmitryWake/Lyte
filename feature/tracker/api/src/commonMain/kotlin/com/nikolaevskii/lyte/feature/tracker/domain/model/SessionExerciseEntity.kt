package com.nikolaevskii.lyte.feature.tracker.domain.model

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity

/**
 * Упражнение внутри сессии. [exercise] — снапшот упражнения-библиотеки на момент старта
 * (сессия самодостаточна и не зависит от того, изменилось ли упражнение позже).
 * Порядок — порядок в списке [WorkoutSessionEntity.exercises].
 */
data class SessionExerciseEntity(
    val id: String,
    val exercise: WorkoutExerciseEntity,
    val sets: List<SessionSetEntity>,
)
