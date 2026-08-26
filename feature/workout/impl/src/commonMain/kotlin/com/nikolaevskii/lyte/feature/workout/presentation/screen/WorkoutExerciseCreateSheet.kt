package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheet
import com.nikolaevskii.lyte.core.design.component.picker.LyteAccentPicker
import com.nikolaevskii.lyte.core.design.component.picker.LyteExerciseIconPicker
import com.nikolaevskii.lyte.core.design.component.textfield.LyteTextField
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_description_label
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_description_placeholder
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_error
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_name_label
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_name_placeholder
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_submit
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_title
import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.feature.workout.presentation.model.toExerciseAccent
import com.nikolaevskii.lyte.feature.workout.presentation.model.toExerciseGlyph
import com.nikolaevskii.lyte.feature.workout.presentation.model.toLyteAccent
import com.nikolaevskii.lyte.feature.workout.presentation.model.toLyteGlyph
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorUiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorUiState.ExerciseCreatorContent
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.ExerciseCreatorViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val CreateSheetFieldSpacing = 18.dp
private val CreateSheetBottomPadding = 28.dp
private const val PREVIEW_EXERCISE_ID = "preview"

/**
 * Форма создания упражнения библиотеки (3.3) — открывается кнопкой «Создать новое упражнение» из
 * [WorkoutExercisePickerSheet] и рисуется вместо неё: две шторки одновременно не показываем.
 *
 * У формы собственные [ExerciseCreatorViewModel] и MVI-контракт: она сама пишет упражнение в
 * библиотеку, а наружу отдаёт только созданное упражнение через [onExerciseCreated]. Добавить его в
 * программу и закрыть шторку — дело владельца.
 *
 * Высота — во весь экран: кроме названия и описания форма несёт пикеры цвета и знака, и по
 * контенту она уже занимает почти весь экран, а `Full`-режим шторки заодно разводит клавиатуру.
 * Кнопка «Сохранить» прибита к низу и гаснет при пустом названии и на время записи в библиотеку.
 */
@Composable
fun WorkoutExerciseCreateSheet(
    initialName: String,
    onExerciseCreated: (WorkoutExerciseEntity) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetViewModelStoreOwner {
        val viewModel: ExerciseCreatorViewModel = koinViewModel { parametersOf(initialName) }
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(state.content) {
            if (state.content is ExerciseCreatorContent.Created) {
                onExerciseCreated(state.exercise)
            }
        }

        WorkoutExerciseCreateSheetContent(
            state = state,
            onIntent = viewModel::onIntent,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
        )
    }
}

@Composable
fun WorkoutExerciseCreateSheetContent(
    state: ExerciseCreatorUiState,
    onIntent: (ExerciseCreatorIntent) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LyteBottomSheet(
        title = stringResource(Res.string.workout_details_exercise_create_title),
        onDismissRequest = onDismissRequest,
        bottomBar = {
            // Подложка с тенью отделяет прибитую кнопку от формы, уезжающей под неё, — как в шторке
            // выбора упражнения и в редакторе подходов.
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = LyteTheme.elevation.level2,
                modifier = Modifier.fillMaxWidth(),
            ) {
                LyteButton(
                    text = stringResource(Res.string.workout_details_exercise_create_submit),
                    onClick = { onIntent(ExerciseCreatorIntent.OnCreateClicked) },
                    enabled = (state.content as? ExerciseCreatorContent.Editing)?.isSubmitEnabled == true,
                    fullWidth = true,
                    modifier = Modifier.padding(
                        start = LyteTheme.spacing.s5,
                        end = LyteTheme.spacing.s5,
                        top = LyteTheme.spacing.s3,
                        bottom = CreateSheetBottomPadding,
                    ),
                )
            }
        },
        modifier = modifier,
    ) {
        // verticalScroll на случай, когда клавиатура вместе с многострочным описанием не оставляет
        // форме высоты: шторка своего скролла не имеет.
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LyteTheme.spacing.s5),
        ) {
            LyteTextField(
                value = state.exercise.name,
                onValueChange = { name -> onIntent(ExerciseCreatorIntent.OnNameChanged(name)) },
                label = stringResource(Res.string.workout_details_exercise_create_name_label),
                placeholder = stringResource(Res.string.workout_details_exercise_create_name_placeholder),
                modifier = Modifier.fillMaxWidth(),
            )
            LyteTextField(
                value = state.exercise.description.orEmpty(),
                onValueChange = { description -> onIntent(ExerciseCreatorIntent.OnDescriptionChanged(description)) },
                label = stringResource(Res.string.workout_details_exercise_create_description_label),
                placeholder = stringResource(Res.string.workout_details_exercise_create_description_placeholder),
                multiline = true,
                modifier = Modifier.fillMaxWidth().padding(top = CreateSheetFieldSpacing),
            )
            LyteAccentPicker(
                value = state.exercise.accent.toLyteAccent(),
                onChange = { selected -> onIntent(ExerciseCreatorIntent.OnAccentChanged(selected.toExerciseAccent())) },
                modifier = Modifier.padding(top = CreateSheetFieldSpacing),
            )
            // Пикер знаков перекрашивается вслед за выбранным цветом — поэтому accent сюда тоже идёт.
            LyteExerciseIconPicker(
                value = state.exercise.glyph.toLyteGlyph(),
                accent = state.exercise.accent.toLyteAccent(),
                onChange = { selected -> onIntent(ExerciseCreatorIntent.OnGlyphChanged(selected.toExerciseGlyph())) },
                modifier = Modifier.padding(top = CreateSheetFieldSpacing),
            )
            (state.content as? ExerciseCreatorContent.Editing)?.error?.let {
                Text(
                    text = stringResource(Res.string.workout_details_exercise_create_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = LyteTheme.spacing.s3),
                )
            }
        }
    }
}

@Composable
@Preview
private fun WorkoutExerciseCreateSheetContentPreview() {
    LyteTheme {
        WorkoutExerciseCreateSheetContent(
            state = ExerciseCreatorUiState(
                exercise = previewExercise(
                    name = "Жим гантелей на наклонной",
                    accent = ExerciseAccent.Teal,
                    glyph = ExerciseGlyph.DumbbellPress,
                ),
                content = ExerciseCreatorContent.Editing(isSubmitEnabled = true),
            ),
            onIntent = {},
            onDismissRequest = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutExerciseCreateSheetContentEmptyPreview() {
    LyteTheme {
        WorkoutExerciseCreateSheetContent(
            state = ExerciseCreatorUiState(exercise = previewExercise(name = "")),
            onIntent = {},
            onDismissRequest = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutExerciseCreateSheetContentErrorPreview() {
    LyteTheme {
        WorkoutExerciseCreateSheetContent(
            state = ExerciseCreatorUiState(
                exercise = previewExercise(name = "Жим стоя"),
                content = ExerciseCreatorContent.Editing(isSubmitEnabled = true, error = LyteError.Storage),
            ),
            onIntent = {},
            onDismissRequest = {},
        )
    }
}

private fun previewExercise(
    name: String,
    accent: ExerciseAccent = ExerciseAccent.Default,
    glyph: ExerciseGlyph = ExerciseGlyph.Default,
): WorkoutExerciseEntity =
    WorkoutExerciseEntity(id = PREVIEW_EXERCISE_ID, name = name, accent = accent, glyph = glyph)
