package com.nikolaevskii.lyte.core.session.domain.model

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity

/**
 * Упражнение внутри сессии. [exercise] — упражнение-библиотека, прочитанное живым по `exercise_id`:
 * имя, описание и маркер в истории всегда те же, что в библиотеке сейчас. Снапшотится только
 * программа ([SessionProgramEntity]) — её можно удалить, упражнение же архивируется и остаётся
 * доступным по id. Порядок — порядок в списке [WorkoutSessionEntity.exercises].
 */
data class SessionExerciseEntity(
    val id: String,
    val exercise: WorkoutExerciseEntity,
    val sets: List<SessionSetEntity>,
)
