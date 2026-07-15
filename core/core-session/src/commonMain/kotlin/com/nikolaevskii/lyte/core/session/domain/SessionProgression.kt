package com.nikolaevskii.lyte.core.session.domain

import com.nikolaevskii.lyte.core.session.domain.model.SessionExerciseEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity

/**
 * Правила прогрессии активной сессии (спека 4.3). Чистые функции без состояния: из одних и тех же
 * данных БД всегда получается один и тот же ответ, поэтому автопереход между упражнениями не требует
 * записи в БД и корректно восстанавливается после смерти процесса.
 */

/**
 * Эффективное текущее упражнение: вручную выбранное ([WorkoutSessionEntity.currentExerciseId]), пока
 * у него есть незакрытые подходы, иначе первое по порядку упражнение с незакрытыми подходами.
 * `null` — все подходы сессии разрешены (состояние «все подходы выполнены»).
 */
fun WorkoutSessionEntity.effectiveCurrentExercise(): SessionExerciseEntity? {
    val manuallySelected = currentExerciseId
        ?.let { exerciseId -> exercises.firstOrNull { exercise -> exercise.id == exerciseId } }
        ?.takeIf { exercise -> exercise.hasPendingSets() }
    return manuallySelected ?: exercises.firstOrNull { exercise -> exercise.hasPendingSets() }
}

/** Текущий подход упражнения — первый без результата; `null`, если все подходы разрешены. */
fun SessionExerciseEntity.currentSet(): SessionSetEntity? =
    sets.firstOrNull { set -> set.result == null }

/** Есть ли в упражнении незакрытые подходы. Упражнение без подходов считается закрытым. */
fun SessionExerciseEntity.hasPendingSets(): Boolean =
    sets.any { set -> set.result == null }
