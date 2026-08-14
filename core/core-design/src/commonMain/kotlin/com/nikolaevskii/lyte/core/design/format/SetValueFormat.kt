package com.nikolaevskii.lyte.core.design.format

import androidx.compose.runtime.Composable
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_reps
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import org.jetbrains.compose.resources.stringResource

/**
 * Единый формат значения подхода: «10×60 кг» при заданном весе, иначе «10 повт» (свой вес).
 * Одна реализация на все компоненты системы — строка подхода на экране тренировки, ориентиры
 * фокус-карточки и дифф деталей сессии обязаны писать одно и то же одинаково.
 */
@Composable
internal fun lyteSetValueLabel(reps: Int, weight: Double?): String =
    if (weight != null && weight > 0.0) {
        "$reps×${formatWeight(weight)} " + stringResource(Res.string.diff_weight)
    } else {
        "$reps " + stringResource(Res.string.diff_reps)
    }
