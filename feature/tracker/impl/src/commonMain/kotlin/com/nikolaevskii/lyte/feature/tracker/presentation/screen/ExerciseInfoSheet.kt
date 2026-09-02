package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.mark.LyteExerciseMark
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheet
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheetHeight
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.format.LyteSetValueFormat
import com.nikolaevskii.lyte.core.design.format.lyteSetValueLabel
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_set_count
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewExerciseUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewIntent
import org.jetbrains.compose.resources.pluralStringResource

private val ExerciseInfoMarkSize = 52.dp
private val ExerciseInfoHeaderGap = 14.dp
private val ExerciseInfoDescriptionTextSize = 13.5.sp

private val ExerciseInfoSetPaddingHorizontal = 14.dp
private val ExerciseInfoSetPaddingVertical = 8.dp

/** Шкала строки подхода совпадает с `LyteTrackSetRow`: одно и то же число на 4.2 и 4.3 читается одинаково. */
private val ExerciseInfoSetIndexWidth = 12.dp
private val ExerciseInfoSetIndexTextSize = 12.sp
private val ExerciseInfoSetValueTextSize = 14.5.sp
private val ExerciseInfoSetValueTracking = (-0.2).sp
private val ExerciseInfoSetLineHeight = 18.sp

private val previewSets = listOf(
    LyteSetValue(reps = 8, weight = 70.0),
    LyteSetValue(reps = 8, weight = 80.0),
    LyteSetValue(reps = 6, weight = 85.0),
)

/**
 * Шторка с описанием упражнения на превью программы (кадры `preview-exercise`,
 * `preview-exercise-bw`): маркер и описание, а под ними — плановые подходы числами. В карточке 4.2
 * план свёрнут в трек и счётчик, точные числа живут здесь, на касание глубже.
 *
 * Высота — по контенту: подходов у упражнения единицы, полноэкранная шторка ради трёх строк
 * выглядела бы отдельным экраном. Скролл — обычная колонка, как в [ProgramPickerSheet]: у ленивого
 * списка в шторке по высоте контента нет ограничения, от которого он мог бы считать окно.
 *
 * Своей ViewModel у шторки нет — состав уже загружен экраном, сюда приходит готовая модель, а
 * закрытие идёт тем же каналом интентов, что и остальные действия экрана.
 */
@Composable
internal fun ExerciseInfoSheet(
    exercise: WorkoutPreviewExerciseUiModel,
    onIntent: (WorkoutPreviewIntent) -> Unit,
) {
    LyteBottomSheet(
        title = exercise.name,
        onDismissRequest = { onIntent(WorkoutPreviewIntent.OnExerciseInfoDismissed) },
        height = LyteBottomSheetHeight.WrapContent,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s4),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LyteTheme.spacing.s5)
                .padding(bottom = LyteTheme.spacing.s3),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ExerciseInfoHeaderGap),
            ) {
                // Название упражнения уже стоит заголовком шторки, поэтому маркер здесь — молча.
                LyteExerciseMark(
                    accent = exercise.accent,
                    glyph = exercise.glyph,
                    size = ExerciseInfoMarkSize,
                )
                // Описание задают не всем упражнениям: без него в ряду остаётся один маркер.
                if (!exercise.description.isNullOrBlank()) {
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodyMedium
                            .copy(fontSize = ExerciseInfoDescriptionTextSize),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s2)) {
                LyteOverline(
                    text = pluralStringResource(
                        Res.plurals.workout_preview_set_count,
                        exercise.sets.size,
                        exercise.sets.size,
                    ),
                )
                exercise.sets.forEachIndexed { index, value ->
                    ExerciseInfoSetRow(number = index + 1, value = value)
                }
            }
        }
    }
}

@Composable
private fun ExerciseInfoSetRow(
    number: Int,
    value: LyteSetValue,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s2),
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.large,
            )
            .padding(
                horizontal = ExerciseInfoSetPaddingHorizontal,
                vertical = ExerciseInfoSetPaddingVertical,
            ),
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium
                .copy(
                    fontSize = ExerciseInfoSetIndexTextSize,
                    lineHeight = ExerciseInfoSetLineHeight,
                    letterSpacing = 0.sp,
                )
                .withTabularNums(),
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            // Минимум, а не фиксированная ширина: двузначный номер подхода не должен обрезаться.
            modifier = Modifier
                .alignByBaseline()
                .widthIn(min = ExerciseInfoSetIndexWidth),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = lyteSetValueLabel(value = value, format = LyteSetValueFormat.Compact),
            style = MaterialTheme.typography.labelLarge
                .copy(
                    fontSize = ExerciseInfoSetValueTextSize,
                    lineHeight = ExerciseInfoSetLineHeight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = ExerciseInfoSetValueTracking,
                )
                .withTabularNums(),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

@Composable
@Preview
private fun ExerciseInfoSheetPreview() {
    LyteTheme {
        ExerciseInfoSheet(
            exercise = WorkoutPreviewExerciseUiModel(
                number = 1,
                name = "Жим лёжа",
                description = "Штанга, горизонтальная скамья",
                sets = previewSets,
                accent = LyteAccent.Indigo,
                glyph = LyteExerciseGlyph.BenchPress,
            ),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ExerciseInfoSheetBodyweightPreview() {
    LyteTheme {
        ExerciseInfoSheet(
            exercise = WorkoutPreviewExerciseUiModel(
                number = 2,
                name = "Отжимания на брусьях",
                description = "Брусья, наклон корпуса вперёд",
                sets = listOf(LyteSetValue(reps = 12), LyteSetValue(reps = 12), LyteSetValue(reps = 10)),
                accent = LyteAccent.Amber,
                glyph = LyteExerciseGlyph.Rack,
            ),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ExerciseInfoSheetNoDescriptionPreview() {
    LyteTheme {
        ExerciseInfoSheet(
            exercise = WorkoutPreviewExerciseUiModel(
                number = 3,
                name = "Растяжка",
                description = null,
                sets = listOf(LyteSetValue(reps = 1)),
                accent = LyteAccent.Teal,
                glyph = LyteExerciseGlyph.Stretch,
            ),
            onIntent = {},
        )
    }
}
