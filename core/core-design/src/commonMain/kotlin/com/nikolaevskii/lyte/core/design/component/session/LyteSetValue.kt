package com.nikolaevskii.lyte.core.design.component.session

/**
 * Значение подхода — повторы и вес.
 *
 * [weight] `null` — упражнение со своим весом: строка печатает «12 повт», а не «12×0 кг». Ноль как
 * признак «без веса» намеренно не используется: в домене вес тоже nullable, и второе кодирование
 * того же факта рано или поздно разъехалось бы с первым.
 */
data class LyteSetValue(
    val reps: Int,
    val weight: Double? = null,
)
