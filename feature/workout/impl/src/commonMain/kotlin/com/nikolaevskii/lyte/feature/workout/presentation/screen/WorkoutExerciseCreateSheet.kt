package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheetHeight
import com.nikolaevskii.lyte.core.design.component.textfield.LyteTextField
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_description_label
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_description_placeholder
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_name_label
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_submit
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercise_create_title
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorUiState
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.ExerciseCreatorViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val CreateSheetFieldSpacing = 14.dp
private val CreateSheetSubmitTopSpacing = 18.dp
private const val PREVIEW_EXERCISE_ID = "preview"

/**
 * Форма создания упражнения библиотеки (3.3) — открывается кнопкой «Создать новое упражнение» из
 * [WorkoutExercisePickerSheet] и рисуется вместо неё: две шторки одновременно не показываем.
 *
 * У формы собственные [ExerciseCreatorViewModel] и MVI-контракт: она сама пишет упражнение в
 * библиотеку, а наружу отдаёт только созданное упражнение через [onExerciseCreated]. Добавить его в
 * программу и закрыть шторку — дело владельца.
 *
 * Шторка открывается по высоте контента ([LyteBottomSheetHeight.WrapContent]) — полей всего два.
 * Кнопка «Создать» прибита к её низу и гаснет при пустом названии и на время записи в библиотеку.
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

        LaunchedEffect(state.isCreated) {
            if (state.isCreated) {
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
        height = LyteBottomSheetHeight.WrapContent,
        bottomBar = {
            LyteButton(
                text = stringResource(Res.string.workout_details_exercise_create_submit),
                onClick = { onIntent(ExerciseCreatorIntent.OnCreateClicked) },
                enabled = state.isSubmitEnabled,
                fullWidth = true,
                modifier = Modifier.padding(
                    start = LyteTheme.spacing.s5,
                    end = LyteTheme.spacing.s5,
                    top = CreateSheetSubmitTopSpacing,
                    bottom = LyteTheme.spacing.s5,
                ),
            )
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
            state.errorMessage?.let { message ->
                Text(
                    text = message,
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
                exercise = previewExercise(name = "Жим гантелей на наклонной"),
                isSubmitEnabled = true,
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
                isSubmitEnabled = true,
                errorMessage = "Не удалось сохранить упражнение",
            ),
            onIntent = {},
            onDismissRequest = {},
        )
    }
}

private fun previewExercise(name: String): WorkoutExerciseEntity =
    WorkoutExerciseEntity(id = PREVIEW_EXERCISE_ID, name = name)
