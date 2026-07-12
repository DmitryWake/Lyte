package com.nikolaevskii.lyte.feature.tracker.presentation.util

/** Целочисленный вес рендерится без дробной части («60», а не «60.0»), дробный — как есть («62.5»). */
internal fun formatWeight(weight: Double): String {
    val rounded = weight.toLong()
    return if (weight == rounded.toDouble()) rounded.toString() else weight.toString()
}
