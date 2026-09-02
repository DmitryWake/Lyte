package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.card.LyteListRow
import com.nikolaevskii.lyte.core.design.component.card.LyteListRowLeading
import com.nikolaevskii.lyte.core.design.component.feedback.LyteEmptyState
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheet
import com.nikolaevskii.lyte.core.design.component.textfield.LyteTextField
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_picker_create
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_picker_empty_hint
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_picker_error
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_picker_empty_message
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_picker_not_found_hint
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_picker_not_found_title
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_picker_search_placeholder
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_picker_title
import com.nikolaevskii.lyte.feature.workout.presentation.model.ExercisePickerResult
import com.nikolaevskii.lyte.feature.workout.presentation.model.toLyteAccent
import com.nikolaevskii.lyte.feature.workout.presentation.model.toLyteGlyph
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExercisePickerIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExercisePickerUiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExercisePickerUiState.ExercisePickerContent
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.ExercisePickerViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val PickerSearchBottomPadding = 10.dp
private val PickerBottomBarPadding = 28.dp

private val previewLibrary = listOf(
    WorkoutExerciseEntity(
        id = "1",
        name = "Приседания со штангой",
        description = "Штанга на верхней части спины, присед до параллели бёдер с полом.",
        accent = ExerciseAccent.Lime,
        glyph = ExerciseGlyph.Squat,
    ),
    WorkoutExerciseEntity(
        id = "2",
        name = "Становая тяга",
        description = "Подъём штанги с пола за счёт разгибания бёдер и спины, руки прямые.",
        accent = ExerciseAccent.Amber,
        glyph = ExerciseGlyph.Deadlift,
    ),
    WorkoutExerciseEntity(
        id = "3",
        name = "Жим лёжа",
        description = "Жим штанги от середины груди лёжа на горизонтальной скамье.",
        accent = ExerciseAccent.Indigo,
        glyph = ExerciseGlyph.BenchPress,
    ),
    WorkoutExerciseEntity(
        id = "4",
        name = "Подтягивания",
        description = null,
        accent = ExerciseAccent.Coral,
        glyph = ExerciseGlyph.PullUp,
    ),
)

/**
 * Шторка выбора упражнения из библиотеки (3.3). Строка поиска закреплена сверху (`topContent`
 * шторки), кнопка создания — снизу (`bottomBar`): ни та ни другая не должны уезжать вместе со
 * списком. Библиотека может вырасти до сотен упражнений, поэтому список — [LazyColumn]; шторка
 * своего скролла не имеет, его реализует потребитель.
 *
 * У шторки собственные [ExercisePickerViewModel] и MVI-контракт, а наружу она отдаёт только
 * результат — через [onExercisePicked] / [onCreateExerciseRequested]. Владелец (экран редактора
 * программы) решает, что с ним делать и когда шторку закрыть.
 *
 * `koinViewModel()` вызывается внутри [SheetViewModelStoreOwner], а не в параметрах функции: стор
 * должен быть подменён выше по дереву, иначе ViewModel переживёт закрытие шторки.
 */
@Composable
fun WorkoutExercisePickerSheet(
    onExercisePicked: (WorkoutExerciseEntity) -> Unit,
    onCreateExerciseRequested: (String) -> Unit,
    onDismissRequest: () -> Unit,
    initialQuery: String = "",
    modifier: Modifier = Modifier,
) {
    SheetViewModelStoreOwner {
        val viewModel: ExercisePickerViewModel = koinViewModel { parametersOf(initialQuery) }
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(state.result) {
            when (val result = state.result) {
                is ExercisePickerResult.Picked -> onExercisePicked(result.exercise)
                is ExercisePickerResult.CreationRequested -> onCreateExerciseRequested(result.name)
                null -> Unit
            }
        }

        WorkoutExercisePickerSheetContent(
            state = state,
            onIntent = viewModel::onIntent,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
        )
    }
}

@Composable
fun WorkoutExercisePickerSheetContent(
    state: ExercisePickerUiState,
    onIntent: (ExercisePickerIntent) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LyteBottomSheet(
        title = stringResource(Res.string.workout_details_picker_title),
        onDismissRequest = onDismissRequest,
        topContent = {
            LyteTextField(
                value = state.query,
                onValueChange = { query -> onIntent(ExercisePickerIntent.OnQueryChanged(query)) },
                placeholder = stringResource(Res.string.workout_details_picker_search_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LyteTheme.spacing.s5,
                        end = LyteTheme.spacing.s5,
                        bottom = PickerSearchBottomPadding,
                    ),
            )
        },
        bottomBar = {
            // Подложка с тенью отделяет прибитую кнопку от списка, уезжающего под неё.
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = LyteTheme.elevation.level2,
                modifier = Modifier.fillMaxWidth(),
            ) {
                LyteButton(
                    text = stringResource(Res.string.workout_details_picker_create),
                    onClick = { onIntent(ExercisePickerIntent.OnCreateExerciseClicked) },
                    icon = LyteIcons.Plus,
                    fullWidth = true,
                    modifier = Modifier.padding(
                        start = LyteTheme.spacing.s5,
                        end = LyteTheme.spacing.s5,
                        top = LyteTheme.spacing.s3,
                        bottom = PickerBottomBarPadding,
                    ),
                )
            }
        },
        modifier = modifier,
    ) {
        when (val content = state.content) {
            ExercisePickerContent.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is ExercisePickerContent.Error -> Text(
                text = stringResource(Res.string.workout_details_picker_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s3),
            )

            ExercisePickerContent.EmptyLibrary ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LyteEmptyState(
                        icon = LyteIcons.Dumbbell,
                        message = stringResource(Res.string.workout_details_picker_empty_message),
                        hint = stringResource(Res.string.workout_details_picker_empty_hint),
                    )
                }

            // Пустой результат поиска — то же полноразмерное состояние, что и пустая библиотека,
            // но со своей иконкой: в макете 3.3 это один и тот же блок, а `SearchX` заведён в ДС
            // именно под него.
            ExercisePickerContent.NotFound ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LyteEmptyState(
                        icon = LyteIcons.SearchX,
                        message = stringResource(Res.string.workout_details_picker_not_found_title),
                        hint = stringResource(Res.string.workout_details_picker_not_found_hint),
                    )
                }

            is ExercisePickerContent.Exercises -> LazyColumn(
                contentPadding = PaddingValues(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s2),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items = content.exercises, key = { exercise -> exercise.id }) { exercise ->
                    LyteListRow(
                        title = exercise.name,
                        subtitle = exercise.description,
                        leading = LyteListRowLeading.Mark(
                            accent = exercise.accent.toLyteAccent(),
                            glyph = exercise.glyph.toLyteGlyph(),
                        ),
                        onClick = { onIntent(ExercisePickerIntent.OnExerciseClicked(exercise.id)) },
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun WorkoutExercisePickerSheetContentPreview() {
    LyteTheme {
        WorkoutExercisePickerSheetContent(
            state = ExercisePickerUiState(content = ExercisePickerContent.Exercises(previewLibrary)),
            onIntent = {},
            onDismissRequest = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutExercisePickerSheetContentNotFoundPreview() {
    LyteTheme {
        WorkoutExercisePickerSheetContent(
            state = ExercisePickerUiState(query = "Жим Арнольда", content = ExercisePickerContent.NotFound),
            onIntent = {},
            onDismissRequest = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutExercisePickerSheetContentEmptyLibraryPreview() {
    LyteTheme {
        WorkoutExercisePickerSheetContent(
            state = ExercisePickerUiState(content = ExercisePickerContent.EmptyLibrary),
            onIntent = {},
            onDismissRequest = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutExercisePickerSheetContentLoadingPreview() {
    LyteTheme {
        WorkoutExercisePickerSheetContent(state = ExercisePickerUiState(), onIntent = {}, onDismissRequest = {})
    }
}

/** Библиотеку не удалось прочитать: поиск и «Создать упражнение» остаются доступными. */
@Composable
@Preview
private fun WorkoutExercisePickerSheetContentErrorPreview() {
    LyteTheme {
        WorkoutExercisePickerSheetContent(
            state = ExercisePickerUiState(content = ExercisePickerContent.Error(LyteError.Storage)),
            onIntent = {},
            onDismissRequest = {},
        )
    }
}
