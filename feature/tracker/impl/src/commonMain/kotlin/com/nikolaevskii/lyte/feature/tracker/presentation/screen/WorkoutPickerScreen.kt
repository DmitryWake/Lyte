package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.component.card.LyteProgramCard
import com.nikolaevskii.lyte.core.design.component.feedback.LyteEmptyState
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_picker_empty_hint
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_picker_error
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_picker_empty_message
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_picker_exercise_count
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_picker_new_program
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_picker_title
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPickerViewModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutProgramUiModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val BottomCtaPaddingTop = 14.dp
private val BottomCtaPaddingHorizontal = 20.dp
private val BottomCtaPaddingBottom = 30.dp

@Composable
fun WorkoutPickerScreen(
    viewModel: WorkoutPickerViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutPickerContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun WorkoutPickerContent(
    state: WorkoutPickerUiState,
    onIntent: (WorkoutPickerIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            LyteTopBar(
                title = stringResource(Res.string.workout_picker_title),
                size = LyteTopBarSize.Large,
                onBack = { onIntent(WorkoutPickerIntent.OnBack) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (state) {
                    WorkoutPickerUiState.Loading ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                    is WorkoutPickerUiState.Error ->
                        Text(
                            text = stringResource(Res.string.workout_picker_error),
                            modifier = Modifier.align(Alignment.Center),
                        )

                    WorkoutPickerUiState.Empty ->
                        LyteEmptyState(
                            icon = LyteIcons.ClipboardList,
                            message = stringResource(Res.string.workout_picker_empty_message),
                            hint = stringResource(Res.string.workout_picker_empty_hint),
                            modifier = Modifier.align(Alignment.Center),
                        )

                    is WorkoutPickerUiState.Content ->
                        WorkoutPickerProgramList(programs = state.programs, onIntent = onIntent)
                }
            }

            // «Новая программа» прибита к низу только в пустом состоянии — в списке создавать нечего:
            // сюда приходят выбрать уже существующую программу.
            if (state is WorkoutPickerUiState.Empty) {
                WorkoutPickerBottomCta(onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun WorkoutPickerProgramList(
    programs: List<WorkoutProgramUiModel>,
    onIntent: (WorkoutPickerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = LyteTheme.spacing.s5,
            end = LyteTheme.spacing.s5,
            top = LyteTheme.spacing.s1,
            bottom = LyteTheme.spacing.s5,
        ),
        verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s3),
        modifier = modifier.fillMaxSize(),
    ) {
        items(items = programs, key = { it.id }) { program ->
            LyteProgramCard(
                title = program.name,
                subtitle = pluralStringResource(
                    Res.plurals.workout_picker_exercise_count,
                    program.exerciseCount,
                    program.exerciseCount,
                ),
                accent = program.accent,
                glyph = program.glyph,
                onClick = { onIntent(WorkoutPickerIntent.OnProgramClicked(program.id)) },
            )
        }
    }
}

@Composable
private fun WorkoutPickerBottomCta(
    onIntent: (WorkoutPickerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = LyteTheme.elevation.level2,
        modifier = modifier.fillMaxWidth(),
    ) {
        LyteButton(
            text = stringResource(Res.string.workout_picker_new_program),
            onClick = { onIntent(WorkoutPickerIntent.OnCreateProgramClicked) },
            size = LyteButtonSize.Large,
            icon = LyteIcons.Plus,
            fullWidth = true,
            modifier = Modifier.padding(
                top = BottomCtaPaddingTop,
                start = BottomCtaPaddingHorizontal,
                end = BottomCtaPaddingHorizontal,
                bottom = BottomCtaPaddingBottom,
            ),
        )
    }
}

@Composable
@Preview
private fun WorkoutPickerContentPreview() {
    LyteTheme {
        WorkoutPickerContent(
            state = WorkoutPickerUiState.Content(
                programs = listOf(
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
                ),
            ),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutPickerContentEmptyPreview() {
    LyteTheme {
        WorkoutPickerContent(
            state = WorkoutPickerUiState.Empty,
            onIntent = {},
        )
    }
}
