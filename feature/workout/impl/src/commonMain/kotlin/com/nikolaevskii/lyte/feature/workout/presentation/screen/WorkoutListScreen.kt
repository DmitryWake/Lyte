package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.component.card.LyteProgramCard
import com.nikolaevskii.lyte.core.design.component.feedback.LyteDialog
import com.nikolaevskii.lyte.core.design.component.feedback.LyteEmptyState
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.component.navigation.LyteBottomNavigationBarHeight
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_exercise_count
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_delete_error
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_delete_a11y
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_error
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_delete_dialog_description
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_delete_dialog_title
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_empty_hint
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_empty_message
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_new_program
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_title
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutProgramUiModel
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutListViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Кнопка удаления на карточке программы: размер действия карточки из макета 3.1. */
private val ProgramCardActionSize = 38.dp

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
        exerciseCount = 3,
        accent = LyteAccent.Lime,
        glyph = LyteExerciseGlyph.Squat,
    ),
)

@Composable
fun WorkoutListScreen(
    viewModel: WorkoutListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutListContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun WorkoutListContent(
    state: WorkoutListUiState,
    onIntent: (WorkoutListIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            LyteTopBar(title = stringResource(Res.string.workout_list_title), size = LyteTopBarSize.Large)
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state) {
                WorkoutListUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is WorkoutListUiState.Error ->
                    Text(
                        text = stringResource(Res.string.workout_list_error),
                        modifier = Modifier.align(Alignment.Center),
                    )

                WorkoutListUiState.Empty ->
                    LyteEmptyState(
                        icon = LyteIcons.ClipboardList,
                        message = stringResource(Res.string.workout_list_empty_message),
                        hint = stringResource(Res.string.workout_list_empty_hint),
                        actionLabel = stringResource(Res.string.workout_list_new_program),
                        actionIcon = LyteIcons.Plus,
                        onAction = { onIntent(WorkoutListIntent.OnCreateProgramClicked) },
                        modifier = Modifier.align(Alignment.Center),
                    )

                is WorkoutListUiState.Content -> {
                    if (state.actionError != null) {
                        Text(
                            text = stringResource(Res.string.workout_list_delete_error),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(LyteTheme.spacing.s3),
                        )
                    }
                    WorkoutProgramList(programs = state.programs, onIntent = onIntent)
                }
            }
        }

        val pendingDelete = (state as? WorkoutListUiState.Content)?.pendingDelete
        if (pendingDelete != null) {
            LyteDialog(
                title = stringResource(Res.string.workout_list_delete_dialog_title, pendingDelete.name),
                description = stringResource(Res.string.workout_list_delete_dialog_description),
                onConfirm = { onIntent(WorkoutListIntent.OnDeleteConfirmed) },
                onDismissRequest = { onIntent(WorkoutListIntent.OnDeleteDismissed) },
            )
        }
    }
}

@Composable
private fun WorkoutProgramList(
    programs: List<WorkoutProgramUiModel>,
    onIntent: (WorkoutListIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        // Список — корень вкладки «Тренировки», показывается вместе с плавающим bottom-доком: тот не
        // резервирует место через Scaffold.bottomBar (см. App() в :shared), поэтому низ списка сам
        // добавляет LyteBottomNavigationBarHeight, иначе «Новая программа» пряталась бы под доком.
        contentPadding = PaddingValues(
            start = LyteTheme.spacing.s5,
            end = LyteTheme.spacing.s5,
            top = LyteTheme.spacing.s1,
            bottom = LyteBottomNavigationBarHeight,
        ),
        verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s3),
        modifier = modifier.fillMaxSize(),
    ) {
        items(items = programs, key = { it.id }) { program ->
            LyteProgramCard(
                title = program.name,
                subtitle = pluralStringResource(
                    Res.plurals.workout_exercise_count,
                    program.exerciseCount,
                    program.exerciseCount,
                ),
                accent = program.accent,
                glyph = program.glyph,
                onClick = { onIntent(WorkoutListIntent.OnProgramClicked(program.id)) },
                trailing = {
                    LyteIconButton(
                        icon = LyteIcons.Delete,
                        contentDescription = stringResource(Res.string.workout_list_delete_a11y),
                        onClick = { onIntent(WorkoutListIntent.OnDeleteProgramClicked(program.id)) },
                        size = ProgramCardActionSize,
                    )
                },
            )
        }
        item {
            LyteButton(
                text = stringResource(Res.string.workout_list_new_program),
                onClick = { onIntent(WorkoutListIntent.OnCreateProgramClicked) },
                variant = LyteButtonVariant.Tonal,
                icon = LyteIcons.Plus,
                fullWidth = true,
            )
        }
    }
}

@Composable
@Preview
private fun WorkoutListContentPreview() {
    LyteTheme {
        WorkoutListContent(
            state = WorkoutListUiState.Content(programs = previewPrograms),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutListContentEmptyPreview() {
    LyteTheme {
        WorkoutListContent(
            state = WorkoutListUiState.Empty,
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutListContentLoadingPreview() {
    LyteTheme {
        WorkoutListContent(
            state = WorkoutListUiState.Loading,
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutListContentErrorPreview() {
    LyteTheme {
        WorkoutListContent(
            state = WorkoutListUiState.Error(LyteError.Storage),
            onIntent = {},
        )
    }
}

/** Кадр `programs-delete`: диалог подтверждения поверх списка. */
@Composable
@Preview
private fun WorkoutListContentDeleteDialogPreview() {
    LyteTheme {
        WorkoutListContent(
            state = WorkoutListUiState.Content(
                programs = previewPrograms,
                pendingDelete = previewPrograms.first(),
            ),
            onIntent = {},
        )
    }
}
