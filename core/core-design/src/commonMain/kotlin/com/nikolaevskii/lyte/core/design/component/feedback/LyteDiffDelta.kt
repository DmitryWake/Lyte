package com.nikolaevskii.lyte.core.design.component.feedback

import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import kotlin.math.round

/** Отклонение факта от цели: [reps] — в повторениях, [weight] — в килограммах. Знак значим. */
internal data class LyteDiffDelta(
    val reps: Int,
    val weight: Double,
)

/**
 * Насколько факт разошёлся с целью. `null` — показывать нечего: подход не выполнен, цели не было
 * или попали ровно в цель. Именно поэтому у строки «в цель» в v2 нет чипа: сообщать не о чем.
 *
 * Вес сравнивается, только если он есть у обеих сторон: упражнение своего веса не «потеряло 60 кг»
 * оттого, что веса у него нет вовсе.
 */
internal fun lyteDiffDelta(target: LyteSetValue?, actual: LyteSetValue?): LyteDiffDelta? {
    if (target == null || actual == null) {
        return null
    }
    val repsDelta = actual.reps - target.reps
    val targetWeight = target.weight
    val actualWeight = actual.weight
    val weightDelta = if (targetWeight != null && actualWeight != null) {
        roundToTwoDecimals(actualWeight - targetWeight)
    } else {
        0.0
    }
    return if (repsDelta == 0 && weightDelta == 0.0) {
        null
    } else {
        LyteDiffDelta(reps = repsDelta, weight = weightDelta)
    }
}

/** Вычитание double'ов даёт хвосты вида `2.4999999999999996` — округляем до шага веса. */
private fun roundToTwoDecimals(value: Double): Double = round(value * 100) / 100
