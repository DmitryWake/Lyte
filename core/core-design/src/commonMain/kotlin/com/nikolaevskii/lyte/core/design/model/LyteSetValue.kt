package com.nikolaevskii.lyte.core.design.model

/**
 * Значение одного подхода — повторения и вес. [weight] `null` у упражнений своего веса
 * (подтягивания, пресс): это не «вес 0», а «веса нет», и показывать «12×0 кг» нельзя.
 *
 * Числа, а не готовая строка «12×62,5»: по паре значений компонент считает дельту «план→факт»
 * ([com.nikolaevskii.lyte.core.design.component.feedback.LyteDiffRow]) и сам добавляет единицы —
 * они компонентный «хром», а не доменный текст вызывающей фичи.
 */
data class LyteSetValue(
    val reps: Int,
    val weight: Double? = null,
)
