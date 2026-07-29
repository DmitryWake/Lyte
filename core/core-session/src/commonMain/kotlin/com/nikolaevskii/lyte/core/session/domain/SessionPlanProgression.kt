package com.nikolaevskii.lyte.core.session.domain

import com.nikolaevskii.lyte.core.session.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity

/**
 * Прогрессия плана по итогам сессии: цель подхода в программе заменяется тем, что реально сделали.
 * Чистая функция без побочных эффектов — записью занимается репозиторий.
 */

/**
 * Программа с целями, подтянутыми под факты сессии.
 *
 * Правила:
 * - меняются только цели; состав и порядок упражнений, число подходов остаются как в [workout];
 * - подход, который **выполнен**, задаёт новую цель (повторения и вес — как в факте);
 * - подход **пропущенный** или не выполнявшийся цель не меняет;
 * - сессия — снапшот, программу могли отредактировать после старта, поэтому упражнения и подходы
 *   сопоставляются по позициям, и упражнение принимается только если совпал id упражнения-библиотеки;
 *   всё, что не сопоставилось, остаётся с прежней целью;
 * - программа не из этой сессии возвращается без изменений.
 */
fun WorkoutSessionEntity.applyProgressionTo(workout: WorkoutEntity): WorkoutEntity {
    if (workout.id != program.id) {
        return workout
    }
    val sessionExercises = exercises
    val progressedExercises = workout.exercises.mapIndexed { exerciseIndex, plannedExercise ->
        val sessionExercise = sessionExercises.getOrNull(exerciseIndex)
            ?.takeIf { candidate -> candidate.exercise.id == plannedExercise.exercise.id }
            ?: return@mapIndexed plannedExercise
        val progressedReps = plannedExercise.reps.mapIndexed { repIndex, plannedRep ->
            sessionExercise.sets.getOrNull(repIndex)?.achievedValue()?.toRepEntity() ?: plannedRep
        }
        plannedExercise.copy(reps = progressedReps)
    }
    return workout.copy(exercises = progressedExercises)
}

/** Фактическое значение подхода; `null` — подход пропущен или не выполнялся (цель не трогаем). */
private fun SessionSetEntity.achievedValue(): SessionSetValueEntity? =
    (result as? SessionSetResultEntity.Completed)?.actual

private fun SessionSetValueEntity.toRepEntity(): WorkoutRepEntity =
    WorkoutRepEntity(count = count, weight = weight)
