package com.nikolaevskii.lyte.core.design.component.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.format.LyteSetValueFormat
import com.nikolaevskii.lyte.core.design.format.MULTIPLICATION_SIGN
import com.nikolaevskii.lyte.core.design.format.NON_BREAKING_SPACE
import com.nikolaevskii.lyte.core.design.format.formatWeight
import com.nikolaevskii.lyte.core.design.format.lyteSetValueLabel
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_reps
import com.nikolaevskii.lyte.core.design.generated.resources.diff_skipped
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import com.nikolaevskii.lyte.core.design.generated.resources.set_target
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import kotlin.math.abs
import org.jetbrains.compose.resources.stringResource

private const val SET_VALUE_SEPARATOR = " · "
private const val SIGN_POSITIVE = "+"

/** Настоящий минус (U+2212), а не дефис: в табличных цифрах дефис короче и сбивает колонку. */
private const val SIGN_NEGATIVE = "−"

private val DiffRowPaddingHorizontal = 14.dp
private val DiffRowPaddingVertical = 11.dp
private val DiffRowGap = 12.dp
private val DiffRowColumnGap = 7.dp
private val DiffValueGap = 6.dp
private val DiffIndexWidth = 14.dp
private val DiffIndexTextSize = 12.sp
private val DiffValueTextSize = 17.sp
private val DiffValueTracking = (-0.3).sp
private val DiffUnitTextSize = 11.sp
private val DiffSkippedTextSize = 14.sp
private val DiffDeltaTextSize = 12.5.sp
private val DiffDeltaPaddingHorizontal = 9.dp
private val DiffDeltaPaddingVertical = 3.dp

/** Заметка выровнена под значение, а не под номер: колонка чисел остаётся сплошной. */
private val DiffNoteIndent = 26.dp

private const val DiffIndexAlpha = 0.6f
private const val DiffUnitAlpha = 0.7f
private const val DiffNoteAlpha = 0.8f
private const val DiffDeltaContainerAlpha = 0.12f

/**
 * Строка результата подхода в деталях сессии. Факт показан **один раз и крупно** («12×62,5 кг»), а
 * сравнение с целью выражено чипом-дельтой («+2 повт · +2,5 кг»): цель читается тоном строки, и
 * повторять её числами незачем. У подхода ровно в цель чипа нет — сообщать не о чем.
 *
 * [tone] — общий словарь исходов системы ([LyteProgressTone]), тот же, что у строки подхода на
 * экране тренировки и у трека сводки: одно и то же событие обязано выглядеть одинаково везде.
 * [LyteProgressTone.Skipped] рисует «пропущено» вместо чисел, [LyteProgressTone.Todo] — цель
 * (в завершённой сессии не встречается, но исход из словаря не выкидывается).
 *
 * [note] — свободный текст, написанный между подходами: длина не ограничена, поэтому заметка
 * **всегда** идёт отдельной строкой под числами и никогда не обрезается.
 */
@Composable
fun LyteDiffRow(
    index: Int,
    tone: LyteProgressTone,
    target: LyteSetValue? = null,
    actual: LyteSetValue? = null,
    note: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = diffColors(tone)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = colors.background,
        contentColor = colors.foreground,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DiffRowColumnGap),
            modifier = Modifier.padding(horizontal = DiffRowPaddingHorizontal, vertical = DiffRowPaddingVertical),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DiffRowGap),
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = DiffIndexTextSize).withTabularNums(),
                    color = colors.foreground.copy(alpha = DiffIndexAlpha),
                    // Минимум, а не фиксированная ширина: двузначный номер подхода не должен обрезаться.
                    modifier = Modifier.widthIn(min = DiffIndexWidth),
                )
                DiffRowFact(
                    tone = tone,
                    target = target,
                    actual = actual,
                    foreground = colors.foreground,
                    modifier = Modifier.weight(1f),
                )
                if (tone != LyteProgressTone.Skipped) {
                    lyteDiffDelta(target = target, actual = actual)?.let { delta ->
                        DiffDeltaChip(delta = delta, foreground = colors.foreground)
                    }
                }
            }
            note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.foreground.copy(alpha = DiffNoteAlpha),
                    modifier = Modifier.padding(start = DiffNoteIndent),
                )
            }
        }
    }
}

@Composable
private fun DiffRowFact(
    tone: LyteProgressTone,
    target: LyteSetValue?,
    actual: LyteSetValue?,
    foreground: Color,
    modifier: Modifier = Modifier,
) {
    when {
        tone == LyteProgressTone.Todo -> Text(
            text = stringResource(
                Res.string.set_target,
                target
                    ?.let { value -> lyteSetValueLabel(value = value, format = LyteSetValueFormat.Compact) }
                    .orEmpty(),
            ),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = DiffSkippedTextSize).withTabularNums(),
            color = foreground,
            modifier = modifier,
        )

        // Тон Skipped — и он же страховка: подход без факта нечем показать, кроме «пропущено».
        tone == LyteProgressTone.Skipped || actual == null -> Text(
            text = stringResource(Res.string.diff_skipped),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = DiffSkippedTextSize,
                fontWeight = FontWeight.Medium,
            ),
            color = foreground,
            modifier = modifier,
        )

        else -> Row(
            horizontalArrangement = Arrangement.spacedBy(DiffValueGap),
            modifier = modifier,
        ) {
            Text(
                text = actualNotation(actual),
                style = MaterialTheme.typography.bodyLarge
                    .copy(
                        fontSize = DiffValueTextSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = DiffValueTracking,
                    )
                    .withTabularNums(),
                color = foreground,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = stringResource(if (actual.hasLoadedWeight()) Res.string.diff_weight else Res.string.diff_reps),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = DiffUnitTextSize,
                    fontWeight = FontWeight.Medium,
                ),
                color = foreground.copy(alpha = DiffUnitAlpha),
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

@Composable
private fun DiffDeltaChip(
    delta: LyteDiffDelta,
    foreground: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = LyteTheme.extendedShapes.full,
        color = foreground.copy(alpha = DiffDeltaContainerAlpha),
        contentColor = foreground,
    ) {
        Text(
            text = deltaLabel(delta),
            style = MaterialTheme.typography.labelMedium
                .copy(fontSize = DiffDeltaTextSize, fontWeight = FontWeight.SemiBold)
                .withTabularNums(),
            modifier = Modifier.padding(
                horizontal = DiffDeltaPaddingHorizontal,
                vertical = DiffDeltaPaddingVertical,
            ),
        )
    }
}

/**
 * Свой вес определяется тем же предикатом, что и в [setValueLabel]: `0.0` — это «без отягощения», а
 * не «ноль килограммов». Иначе строка факта напишет «12×0 кг» там, где строка цели в этом же
 * компоненте напишет «12 повт».
 */
private fun LyteSetValue.hasLoadedWeight(): Boolean = weight != null && weight > 0.0

/**
 * Нотация собирается здесь, а не берётся у [setValueLabel]: единица рисуется отдельным `Text` со
 * своим кеглем и выравниванием по базовой линии, готовую строку так не разложить. Разделитель и
 * округление при этом общие — расходиться им нельзя.
 */
private fun actualNotation(actual: LyteSetValue): String =
    if (actual.hasLoadedWeight()) {
        "${actual.reps}$MULTIPLICATION_SIGN${formatWeight(checkNotNull(actual.weight))}"
    } else {
        actual.reps.toString()
    }

@Composable
private fun deltaLabel(delta: LyteDiffDelta): String {
    val parts = buildList {
        if (delta.reps != 0) {
            val reps = "${sign(delta.reps.toDouble())}${abs(delta.reps)}"
            add(reps + NON_BREAKING_SPACE + stringResource(Res.string.diff_reps))
        }
        if (delta.weight != 0.0) {
            val weight = "${sign(delta.weight)}${formatWeight(abs(delta.weight))}"
            add(weight + NON_BREAKING_SPACE + stringResource(Res.string.diff_weight))
        }
    }
    return parts.joinToString(SET_VALUE_SEPARATOR)
}

private fun sign(value: Double): String = if (value > 0) SIGN_POSITIVE else SIGN_NEGATIVE

@Composable
private fun diffColors(tone: LyteProgressTone): DiffRowColors {
    val extended = LyteTheme.extendedColors
    return when (tone) {
        LyteProgressTone.Met -> DiffRowColors(extended.diffMet, extended.diffMetBg)
        LyteProgressTone.Positive -> DiffRowColors(extended.diffPositive, extended.diffPositiveBg)
        LyteProgressTone.Negative -> DiffRowColors(extended.diffNegative, extended.diffNegativeBg)
        LyteProgressTone.Skipped -> DiffRowColors(extended.diffSkipped, extended.diffSkippedBg)
        // Ровно то же, чем «ещё не выполнен» рисуется в строке подхода на экране тренировки.
        LyteProgressTone.Todo -> DiffRowColors(
            foreground = MaterialTheme.colorScheme.outline,
            background = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    }
}

private data class DiffRowColors(
    val foreground: Color,
    val background: Color,
)

@Preview
@Composable
private fun LyteDiffRowPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteDiffRow(
                index = 1,
                tone = LyteProgressTone.Met,
                target = LyteSetValue(reps = 10, weight = 60.0),
                actual = LyteSetValue(reps = 10, weight = 60.0),
            )
            LyteDiffRow(
                index = 2,
                tone = LyteProgressTone.Positive,
                target = LyteSetValue(reps = 10, weight = 60.0),
                actual = LyteSetValue(reps = 12, weight = 62.5),
            )
            LyteDiffRow(
                index = 3,
                tone = LyteProgressTone.Negative,
                target = LyteSetValue(reps = 10, weight = 60.0),
                actual = LyteSetValue(reps = 8, weight = 60.0),
                note = "Последний подход дался тяжело, поясница даёт о себе знать — в следующий раз снижу вес.",
            )
            LyteDiffRow(
                index = 4,
                tone = LyteProgressTone.Skipped,
                target = LyteSetValue(reps = 10, weight = 60.0),
            )
            LyteDiffRow(
                index = 5,
                tone = LyteProgressTone.Positive,
                target = LyteSetValue(reps = 12),
                actual = LyteSetValue(reps = 15),
            )
            LyteDiffRow(
                index = 6,
                tone = LyteProgressTone.Todo,
                target = LyteSetValue(reps = 10, weight = 60.0),
            )
        }
    }
}
