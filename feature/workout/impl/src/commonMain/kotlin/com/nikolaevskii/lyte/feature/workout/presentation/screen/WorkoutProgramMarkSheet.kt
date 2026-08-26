package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheet
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheetHeight
import com.nikolaevskii.lyte.core.design.component.picker.LyteAccentPicker
import com.nikolaevskii.lyte.core.design.component.picker.LyteExerciseIconPicker
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_done
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_mark_title
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.toExerciseAccent
import com.nikolaevskii.lyte.feature.workout.presentation.model.toExerciseGlyph
import com.nikolaevskii.lyte.feature.workout.presentation.model.toLyteAccent
import com.nikolaevskii.lyte.feature.workout.presentation.model.toLyteGlyph
import org.jetbrains.compose.resources.stringResource

private val MarkSheetGap = 18.dp
private val MarkSheetBottomPadding = 12.dp

/**
 * Шторка «Цвет и знак» редактора программы (кадр `program-mark`). Оба пикера стейтлес, а выбор
 * пишется прямо в черновик формы: маркер виден в шапке редактора, пока шторка открыта, поэтому
 * промежуточного «примерочного» состояния у шторки нет.
 *
 * Своей ViewModel нет — приём RD-11/RD-12: у шторки нечего грузить, значит нет ни арма загрузки,
 * ни арма ошибки.
 */
@Composable
fun WorkoutProgramMarkSheet(
    accent: ExerciseAccent,
    glyph: ExerciseGlyph,
    onIntent: (WorkoutDetailsIntent) -> Unit,
) {
    LyteBottomSheet(
        title = stringResource(Res.string.workout_details_mark_title),
        onDismissRequest = { onIntent(WorkoutDetailsIntent.OnMarkSheetDismissed) },
        height = LyteBottomSheetHeight.WrapContent,
        bottomBar = {
            // Кнопка прибита к низу слотом шторки, а не лежит последней в контенте: так же сделаны
            // «Создать упражнение» в шторке библиотеки и «Готово» в редакторе подходов.
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = LyteTheme.elevation.level2,
                modifier = Modifier.fillMaxWidth(),
            ) {
                LyteButton(
                    text = stringResource(Res.string.workout_details_done),
                    onClick = { onIntent(WorkoutDetailsIntent.OnMarkSheetDismissed) },
                    fullWidth = true,
                    modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s4),
                )
            }
        },
    ) {
        // Слот контента шторки идёт во всю ширину (списки в других шторках так и нужны), поэтому
        // поля формы задаёт вызывающая сторона — тем же отступом, что у заголовка шторки.
        Column(
            verticalArrangement = Arrangement.spacedBy(MarkSheetGap),
            modifier = Modifier.padding(
                start = LyteTheme.spacing.s5,
                end = LyteTheme.spacing.s5,
                bottom = MarkSheetBottomPadding,
            ),
        ) {
            LyteAccentPicker(
                value = accent.toLyteAccent(),
                onChange = { selected -> onIntent(WorkoutDetailsIntent.OnAccentChanged(selected.toExerciseAccent())) },
            )
            // Пикер знаков перекрашивается вслед за выбранным цветом — поэтому accent сюда тоже идёт.
            LyteExerciseIconPicker(
                value = glyph.toLyteGlyph(),
                accent = accent.toLyteAccent(),
                onChange = { selected -> onIntent(WorkoutDetailsIntent.OnGlyphChanged(selected.toExerciseGlyph())) },
            )
        }
    }
}

@Composable
@Preview
private fun WorkoutProgramMarkSheetPreview() {
    LyteTheme {
        WorkoutProgramMarkSheet(
            accent = ExerciseAccent.Indigo,
            glyph = ExerciseGlyph.BenchPress,
            onIntent = {},
        )
    }
}
