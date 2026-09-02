package com.nikolaevskii.lyte.core.design.format

import androidx.compose.runtime.Composable
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_reps
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import org.jetbrains.compose.resources.stringResource

/**
 * Неразрывный пробел (U+00A0) между числом и его единицей: «62,5 кг» не должно переноситься так,
 * чтобы «кг» уехало на следующую строку. Только на выводе — в хранимые данные не попадает.
 */
internal const val NON_BREAKING_SPACE = "\u00A0"

internal const val MULTIPLICATION_SIGN = "×"

/**
 * Форма записи значения подхода. Выбирается **по месту вызова**, а не общим правилом:
 *
 * - [Compact] — «10×62,5 кг». Спокойные строки списка подходов, пилюли целей, плановые подходы в
 *   шторке. Смысл узкой строки в том, что семь таких помещаются рядом с фокус-карточкой;
 *   развёрнутый вариант распирает её 36dp.
 * - [Expanded] — «10 повт × 62,5 кг». Ориентиры фокус-карточки («Цель», «В прошлый раз»), где строк
 *   всего две и число нужно прочитать без домысливания, что чему единица.
 */
enum class LyteSetValueFormat { Compact, Expanded }

/**
 * Единый формат значения подхода: обе формы считает одна функция, поэтому разъехаться разделителем
 * или округлением они не могут. У упражнения своего веса ([LyteSetValue.weight] `null` или ноль)
 * форма одна — «10 повт»: умножать не на что.
 *
 * Публичная, потому что фича обязана писать это значение и там, где его рисует не компонент ДС
 * (пилюли целей в шторке упражнений сессии, плановые подходы в шторке описания) — второму
 * форматтеру того же значения в системе места нет.
 */
@Composable
fun lyteSetValueLabel(value: LyteSetValue, format: LyteSetValueFormat): String = setValueLabel(
    value = value,
    format = format,
    repsUnit = stringResource(Res.string.diff_reps),
    weightUnit = stringResource(Res.string.diff_weight),
)

/**
 * Сборка строки без обращения к ресурсам: единицы приходят готовыми, поэтому правило склейки
 * проверяется юнит-тестом, а не только скриншотом.
 */
internal fun setValueLabel(
    value: LyteSetValue,
    format: LyteSetValueFormat,
    repsUnit: String,
    weightUnit: String,
): String {
    val weight = value.weight
    // Число повторов пишется один раз: во второй интерполяции оно молча разошлось бы с первой.
    val reps = value.reps.toString()
    val repsWithUnit = "$reps$NON_BREAKING_SPACE$repsUnit"
    if (weight == null || weight <= 0.0) {
        return repsWithUnit
    }
    val loadedWeight = "${formatWeight(weight)}$NON_BREAKING_SPACE$weightUnit"
    return when (format) {
        LyteSetValueFormat.Compact -> "$reps$MULTIPLICATION_SIGN$loadedWeight"
        LyteSetValueFormat.Expanded -> "$repsWithUnit $MULTIPLICATION_SIGN $loadedWeight"
    }
}
