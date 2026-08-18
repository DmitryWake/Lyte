package com.nikolaevskii.lyte.core.design.format

import androidx.compose.runtime.Composable
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_reps
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import org.jetbrains.compose.resources.stringResource

/**
 * Единый формат значения подхода: «10×60 кг» при заданном весе, иначе «10 повт» (свой вес).
 * Одна реализация на все компоненты системы — строка подхода на экране тренировки, ориентиры
 * фокус-карточки и пилюли целей в шторках обязаны писать одно и то же одинаково, поэтому функция
 * публичная: фича не должна собирать это значение собственными строковыми ресурсами.
 */
@Composable
fun lyteSetValueLabel(value: LyteSetValue): String {
    val weight = value.weight
    return if (weight != null && weight > 0.0) {
        "${value.reps}×${formatWeight(weight)} " + stringResource(Res.string.diff_weight)
    } else {
        "${value.reps} " + stringResource(Res.string.diff_reps)
    }
}
