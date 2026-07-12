package com.nikolaevskii.lyte.feature.tracker.domain.util

import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetOutcomeEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetValueEntity

/**
 * Есть ли у значения реальный вес: `null` или `0` — упражнение со своим весом (bodyweight), вес не
 * участвует ни в отображении, ни в сравнении с целью. Конвенция та же, что у `WorkoutRepEntity`.
 */
val SessionSetValueEntity.hasWeight: Boolean
    get() {
        val weight = weight
        return weight != null && weight > 0.0
    }

/**
 * Итог подхода против цели ([target]); `null` — подход ещё не выполнялся.
 * Единая точка правды для тонов трекинга и диффа истории.
 *
 * Правила: вес сравнивается только для весовой цели ([hasWeight]); [com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetOutcomeEntity.MET] —
 * все сравниваемые измерения ровно в цель; [com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetOutcomeEntity.EXCEEDED] — ни одно не ниже и
 * хотя бы одно выше; [com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetOutcomeEntity.MISSED] — хотя бы одно ниже цели.
 */
fun SessionSetEntity.outcome(): SessionSetOutcomeEntity? = when (val result = result) {
    null -> null
    SessionSetResultEntity.Skipped -> SessionSetOutcomeEntity.SKIPPED
    is SessionSetResultEntity.Completed -> compareToTarget(actual = result.actual, target = target)
}

private fun compareToTarget(
    actual: SessionSetValueEntity,
    target: SessionSetValueEntity,
): SessionSetOutcomeEntity {
    val comparisons = buildList {
        add(actual.count.compareTo(target.count))
        if (target.hasWeight) {
            add((actual.weight ?: 0.0).compareTo(target.weight ?: 0.0))
        }
    }
    return when {
        comparisons.any { comparison -> comparison < 0 } -> SessionSetOutcomeEntity.MISSED
        comparisons.any { comparison -> comparison > 0 } -> SessionSetOutcomeEntity.EXCEEDED
        else -> SessionSetOutcomeEntity.MET
    }
}
