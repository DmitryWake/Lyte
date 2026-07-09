package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.component.card.LyteProgramCard
import com.nikolaevskii.lyte.core.design.component.feedback.LyteDialog
import com.nikolaevskii.lyte.core.design.component.feedback.LyteEmptyState
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_delete_a11y
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_delete_dialog_description
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_delete_dialog_title
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_empty_hint
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_empty_message
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_exercise_count
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_new_program
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_list_title
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutListViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
        val errorMessage = state.errorMessage
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                errorMessage != null && state.programs.isEmpty() ->
                    Text(text = errorMessage, modifier = Modifier.align(Alignment.Center))

                state.isLoading && state.programs.isEmpty() ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                state.programs.isEmpty() ->
                    LyteEmptyState(
                        icon = LyteIcons.ClipboardList,
                        message = stringResource(Res.string.workout_list_empty_message),
                        hint = stringResource(Res.string.workout_list_empty_hint),
                        actionLabel = stringResource(Res.string.workout_list_new_program),
                        onAction = { onIntent(WorkoutListIntent.CreateProgram) },
                        modifier = Modifier.align(Alignment.Center),
                    )

                else -> WorkoutProgramList(programs = state.programs, onIntent = onIntent)
            }
        }

        val pendingDeleteName = state.programs.firstOrNull { it.id == state.pendingDeleteId }?.name
        if (pendingDeleteName != null) {
            LyteDialog(
                title = stringResource(Res.string.workout_list_delete_dialog_title, pendingDeleteName),
                description = stringResource(Res.string.workout_list_delete_dialog_description),
                onConfirm = { onIntent(WorkoutListIntent.ConfirmDelete) },
                onDismissRequest = { onIntent(WorkoutListIntent.CancelDelete) },
            )
        }
    }
}

@Composable
private fun WorkoutProgramList(
    programs: List<WorkoutItemEntity>,
    onIntent: (WorkoutListIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s1),
        verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s3),
        modifier = modifier.fillMaxSize(),
    ) {
        items(items = programs, key = { it.id }) { program ->
            LyteProgramCard(
                title = program.name,
                subtitle = pluralStringResource(
                    Res.plurals.workout_list_exercise_count,
                    program.exerciseCount,
                    program.exerciseCount,
                ),
                onClick = { onIntent(WorkoutListIntent.OpenDetails(program.id)) },
                trailing = {
                    LyteIconButton(
                        icon = LyteIcons.Delete,
                        contentDescription = stringResource(Res.string.workout_list_delete_a11y),
                        onClick = { onIntent(WorkoutListIntent.RequestDelete(program.id)) },
                    )
                },
            )
        }
        item {
            LyteButton(
                text = stringResource(Res.string.workout_list_new_program),
                onClick = { onIntent(WorkoutListIntent.CreateProgram) },
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
            state = WorkoutListUiState(
                programs = listOf(
                    WorkoutItemEntity(id = "1", name = "Push Day", description = null, exerciseCount = 5),
                    WorkoutItemEntity(id = "2", name = "Pull Day", description = null, exerciseCount = 4),
                    WorkoutItemEntity(id = "3", name = "Leg Day", description = null, exerciseCount = 3),
                ),
            ),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutListContentEmptyPreview() {
    LyteTheme {
        WorkoutListContent(
            state = WorkoutListUiState(),
            onIntent = {},
        )
    }
}
