package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.nikolaevskii.lyte.core.design.component.card.LyteExerciseCard
import com.nikolaevskii.lyte.core.design.component.card.LyteExerciseCardVariant
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_error
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_exercise_count
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_set_bodyweight
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_set_count
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_set_weight
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_start
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_summary
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewExerciseUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewSetUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPreviewViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// Общий `@Preview` в commonMain не принимает device/widthDp, поэтому даём превью телефонную ширину
// сами — иначе full-screen Scaffold в панели превью схлопывается до ширины контента и крупный тайтл
// переносится (на реальном устройстве этого нет).
private val PreviewDeviceWidth = 411.dp
private val PreviewDeviceHeight = 914.dp

@Composable
fun WorkoutPreviewScreen(
    programId: String,
    viewModel: WorkoutPreviewViewModel = koinViewModel { parametersOf(programId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutPreviewContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun WorkoutPreviewContent(
    state: WorkoutPreviewUiState,
    onIntent: (WorkoutPreviewIntent) -> Unit,
) {
    val program = state.program
    Scaffold(
        topBar = {
            LyteTopBar(
                title = program?.programName.orEmpty(),
                size = LyteTopBarSize.Large,
                subtitle = program?.let { programSummary(it) },
                onBack = { onIntent(WorkoutPreviewIntent.OnBack) },
            )
        },
        bottomBar = {
            if (program != null) {
                WorkoutPreviewStartBar(
                    enabled = !state.isStarting,
                    onStart = { onIntent(WorkoutPreviewIntent.OnStartClicked) },
                )
            }
        },
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.errorMessage != null -> Text(
                    text = stringResource(Res.string.workout_preview_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5),
                )

                program != null -> WorkoutPreviewExerciseList(
                    exercises = program.exercises,
                    modifier = Modifier.fillMaxSize(),
                )

                else -> CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun WorkoutPreviewExerciseList(
    exercises: List<WorkoutPreviewExerciseUiModel>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = LyteTheme.spacing.s5,
            end = LyteTheme.spacing.s5,
            top = LyteTheme.spacing.s2,
            bottom = LyteTheme.spacing.s3,
        ),
        verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s3),
        modifier = modifier,
    ) {
        items(items = exercises, key = { exercise -> exercise.number }) { exercise ->
            LyteExerciseCard(
                title = exercise.name,
                setLabels = exercise.sets.map { set -> setLabel(set) },
                variant = LyteExerciseCardVariant.Preview(index = exercise.number),
            )
        }
    }
}

@Composable
private fun WorkoutPreviewStartBar(
    enabled: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = LyteTheme.elevation.level2,
        modifier = modifier.fillMaxWidth(),
    ) {
        LyteButton(
            text = stringResource(Res.string.workout_preview_start),
            onClick = onStart,
            size = LyteButtonSize.Large,
            icon = LyteIcons.Play,
            enabled = enabled,
            fullWidth = true,
            modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s4),
        )
    }
}

/** Локализованная сводка «N упражнений · M подходов» из уже посчитанных в модели чисел. */
@Composable
private fun programSummary(program: WorkoutPreviewUiModel): String = stringResource(
    Res.string.workout_preview_summary,
    pluralStringResource(Res.plurals.workout_preview_exercise_count, program.exerciseCount, program.exerciseCount),
    pluralStringResource(Res.plurals.workout_preview_set_count, program.setCount, program.setCount),
)

/** Локализованная подпись подхода: единицы подставляются здесь, число и формат веса — из модели. */
@Composable
private fun setLabel(set: WorkoutPreviewSetUiModel): String = when (set) {
    is WorkoutPreviewSetUiModel.Weighted -> stringResource(Res.string.workout_preview_set_weight, set.reps, set.weight)
    is WorkoutPreviewSetUiModel.Bodyweight -> stringResource(Res.string.workout_preview_set_bodyweight, set.reps)
}

@Composable
@Preview
private fun WorkoutPreviewContentPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            WorkoutPreviewContent(
                state = WorkoutPreviewUiState(
                    isLoading = false,
                    program = WorkoutPreviewUiModel(
                        programName = "Push Day",
                        exerciseCount = 3,
                        setCount = 9,
                        exercises = listOf(
                            WorkoutPreviewExerciseUiModel(
                                number = 1,
                                name = "Жим лёжа",
                                sets = listOf(weighted(8, "70"), weighted(8, "80"), weighted(6, "85")),
                            ),
                            WorkoutPreviewExerciseUiModel(
                                number = 2,
                                name = "Жим гантелей на наклонной",
                                sets = listOf(weighted(10, "24"), weighted(10, "26"), weighted(8, "26")),
                            ),
                            WorkoutPreviewExerciseUiModel(
                                number = 3,
                                name = "Отжимания на брусьях",
                                sets = listOf(bodyweight(12), bodyweight(12), bodyweight(10)),
                            ),
                        ),
                    ),
                ),
                onIntent = {},
            )
        }
    }
}

@Composable
@Preview
private fun WorkoutPreviewContentLoadingPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            WorkoutPreviewContent(
                state = WorkoutPreviewUiState(isLoading = true),
                onIntent = {},
            )
        }
    }
}

private fun weighted(reps: Int, weight: String): WorkoutPreviewSetUiModel =
    WorkoutPreviewSetUiModel.Weighted(reps = reps, weight = weight)

private fun bodyweight(reps: Int): WorkoutPreviewSetUiModel =
    WorkoutPreviewSetUiModel.Bodyweight(reps = reps)
