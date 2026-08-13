package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.runtime.Composable
import com.nikolaevskii.lyte.core.design.format.formatWeight
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_reps
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import org.jetbrains.compose.resources.stringResource

/**
 * Единый формат значения подхода для session-компонентов: «10×60 кг» при заданном весе,
 * иначе «10 повт» (свой вес). Единицы — общий компонентный «хром» (переиспользует строки `LyteDiffRow`).
 */
@Composable
internal fun setValueLabel(value: LyteSetValue): String {
    val weight = value.weight
    return if (weight != null && weight > 0.0) {
        "${value.reps}×${formatWeight(weight)} " + stringResource(Res.string.diff_weight)
    } else {
        "${value.reps} " + stringResource(Res.string.diff_reps)
    }
}
