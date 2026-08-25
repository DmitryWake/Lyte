package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.card.LyteListRow
import com.nikolaevskii.lyte.core.design.component.card.LyteListRowLeading
import com.nikolaevskii.lyte.core.design.component.feedback.LyteEmptyState
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheet
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheetHeight
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.program_picker_empty_hint
import com.nikolaevskii.lyte.feature.tracker.generated.resources.program_picker_empty_message
import com.nikolaevskii.lyte.feature.tracker.generated.resources.program_picker_error
import com.nikolaevskii.lyte.feature.tracker.generated.resources.program_picker_exercise_count
import com.nikolaevskii.lyte.feature.tracker.generated.resources.program_picker_new_program
import com.nikolaevskii.lyte.feature.tracker.generated.resources.program_picker_title
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ProgramPickerUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutProgramUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingIntent
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val previewPrograms = listOf(
    WorkoutProgramUiModel(
        id = "1",
        name = "Push Day",
        exerciseCount = 5,
        accent = LyteAccent.Indigo,
        glyph = LyteExerciseGlyph.BenchPress,
    ),
    WorkoutProgramUiModel(
        id = "2",
        name = "Pull Day",
        exerciseCount = 4,
        accent = LyteAccent.Coral,
        glyph = LyteExerciseGlyph.PullUp,
    ),
    WorkoutProgramUiModel(
        id = "3",
        name = "Leg Day",
        exerciseCount = 6,
        accent = LyteAccent.Lime,
        glyph = LyteExerciseGlyph.Squat,
    ),
)

/**
 * Шторка выбора программы на лендинге (4.1). Высота — по контенту: программ у пользователя единицы,
 * и полноэкранная шторка ради трёх строк выглядела бы как отдельный экран, от которого RD-11 как раз
 * уходит. Список короткий, поэтому обычная колонка со скроллом, а не [androidx.compose.foundation.lazy.LazyColumn]:
 * в шторке по высоте контента у ленивого списка нет ограничения, от которого он мог бы считать окно.
 *
 * Своей ViewModel у шторки нет — состояние и загрузка живут в модели лендинга, а сюда приходит
 * готовый [picker]; закрытие идёт тем же каналом интентов, что и остальные действия экрана.
 */
@Composable
internal fun ProgramPickerSheet(
    picker: ProgramPickerUiModel,
    onIntent: (TrackerLandingIntent) -> Unit,
) {
    LyteBottomSheet(
        title = stringResource(Res.string.program_picker_title),
        onDismissRequest = { onIntent(TrackerLandingIntent.OnPickerDismissed) },
        height = LyteBottomSheetHeight.WrapContent,
    ) {
        when (picker) {
            ProgramPickerUiModel.Loading -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LyteTheme.spacing.s10),
            ) {
                CircularProgressIndicator()
            }

            is ProgramPickerUiModel.Programs -> Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s1),
            ) {
                picker.programs.forEach { program ->
                    LyteListRow(
                        title = program.name,
                        subtitle = pluralStringResource(
                            Res.plurals.program_picker_exercise_count,
                            program.exerciseCount,
                            program.exerciseCount,
                        ),
                        leading = LyteListRowLeading.Mark(accent = program.accent, glyph = program.glyph),
                        onClick = { onIntent(TrackerLandingIntent.OnProgramClicked(program.id)) },
                    )
                }
            }

            ProgramPickerUiModel.Empty -> LyteEmptyState(
                message = stringResource(Res.string.program_picker_empty_message),
                icon = LyteIcons.ClipboardList,
                hint = stringResource(Res.string.program_picker_empty_hint),
                actionLabel = stringResource(Res.string.program_picker_new_program),
                onAction = { onIntent(TrackerLandingIntent.OnCreateProgramClicked) },
                modifier = Modifier.fillMaxWidth(),
            )

            is ProgramPickerUiModel.Error -> Text(
                text = stringResource(Res.string.program_picker_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(LyteTheme.spacing.s5),
            )
        }
    }
}

@Composable
@Preview
private fun ProgramPickerSheetPreview() {
    LyteTheme {
        ProgramPickerSheet(
            picker = ProgramPickerUiModel.Programs(previewPrograms),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ProgramPickerSheetEmptyPreview() {
    LyteTheme {
        ProgramPickerSheet(
            picker = ProgramPickerUiModel.Empty,
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ProgramPickerSheetLoadingPreview() {
    LyteTheme {
        ProgramPickerSheet(
            picker = ProgramPickerUiModel.Loading,
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ProgramPickerSheetErrorPreview() {
    LyteTheme {
        ProgramPickerSheet(
            picker = ProgramPickerUiModel.Error(LyteError.Storage),
            onIntent = {},
        )
    }
}
