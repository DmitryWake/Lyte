package com.nikolaevskii.lyte.core.design.format

/**
 * Единый формат отображения веса: целочисленный — без дробной части («60», а не «60.0»),
 * дробный — как есть («62.5»). Одна реализация на все фичи (они зависят от `:core:core-design`).
 */
fun formatWeight(weight: Double): String {
    val asLong = weight.toLong()
    return if (weight == asLong.toDouble()) asLong.toString() else weight.toString()
}
