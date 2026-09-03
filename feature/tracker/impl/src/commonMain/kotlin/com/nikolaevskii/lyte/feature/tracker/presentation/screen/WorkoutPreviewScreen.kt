package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.component.card.LyteExerciseCard
import com.nikolaevskii.lyte.core.design.component.card.LyteExerciseCardVariant
import com.nikolaevskii.lyte.core.design.component.feedback.LyteEmptyState
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_error
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_error_action
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_gone
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_gone_hint
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_start_error
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_exercise_count
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_set_count
import com.nikolaevskii.lyte.feature.tracker.generated.resources.workout_preview_start
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewExerciseUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.WorkoutPreviewViewModel
import org.jetbrains.compose.resources.StringResource
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
    val content = state as? WorkoutPreviewUiState.Content
    val program = content?.program
    Scaffold(
        topBar = {
            LyteTopBar(
                title = program?.programName.orEmpty(),
                size = LyteTopBarSize.Large,
                // Один факт, а не сводка «N упражнений · M подходов»: сколько подходов в каждом
                // упражнении, показывают треки плана на карточках.
                subtitle = program?.let { loaded ->
                    pluralStringResource(
                        Res.plurals.workout_preview_exercise_count,
                        loaded.exerciseCount,
                        loaded.exerciseCount,
                    )
                },
                onBack = { onIntent(WorkoutPreviewIntent.OnBack) },
            )
        },
        bottomBar = {
            if (content != null) {
                WorkoutPreviewStartBar(content = content, onIntent = onIntent)
            }
        },
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state) {
                WorkoutPreviewUiState.Loading -> CircularProgressIndicator()

                is WorkoutPreviewUiState.Error -> WorkoutPreviewErrorContent(
                    error = state.error,
                    onIntent = onIntent,
                )

                is WorkoutPreviewUiState.Content -> {
                    WorkoutPreviewExerciseList(
                        exercises = state.program.exercises,
                        onIntent = onIntent,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Шторка описания упражнения — оверлей превью, а не отдельный маршрут:
                    // в стеке вкладки её нет.
                    state.exerciseInfo?.let { exercise ->
                        ExerciseInfoSheet(exercise = exercise, onIntent = onIntent)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutPreviewExerciseList(
    exercises: List<WorkoutPreviewExerciseUiModel>,
    onIntent: (WorkoutPreviewIntent) -> Unit,
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
                accent = exercise.accent,
                glyph = exercise.glyph,
                variant = LyteExerciseCardVariant.ReadOnly,
                setCount = exercise.sets.size,
                setsLabel = pluralStringResource(
                    Res.plurals.workout_preview_set_count,
                    exercise.sets.size,
                    exercise.sets.size,
                ),
                onClick = { onIntent(WorkoutPreviewIntent.OnExerciseClicked(exercise.number)) },
            )
        }
    }
}

/**
 * Программы нет — объяснение и выход, а не строка ошибки на пустом экране: кнопки «Начать» в этом
 * арме уже нет, и единственный путь дальше — назад на лендинг.
 */
@Composable
private fun WorkoutPreviewErrorContent(
    error: LyteError,
    onIntent: (WorkoutPreviewIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LyteEmptyState(
        message = stringResource(error.toMessageResource()),
        icon = LyteIcons.ClipboardList,
        hint = error.toHintResource()?.let { hint -> stringResource(hint) },
        actionLabel = stringResource(Res.string.workout_preview_error_action),
        onAction = { onIntent(WorkoutPreviewIntent.OnBack) },
        modifier = modifier,
    )
}

/**
 * Панель старта: кнопка и, если старт не удался, строка ошибки прямо над ней. Баннер живёт здесь, а не
 * оверлеем над списком: непрозрачная панель — его подложка, и он не накрывает состав программы (сам
 * список — `LazyColumn`, поверх которого текст без подложки нечитаем).
 */
@Composable
private fun WorkoutPreviewStartBar(
    content: WorkoutPreviewUiState.Content,
    onIntent: (WorkoutPreviewIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = LyteTheme.elevation.level2,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s4),
        ) {
            if (content.startError != null) {
                Text(
                    text = stringResource(Res.string.workout_preview_start_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = LyteTheme.spacing.s2),
                )
            }
            LyteButton(
                text = stringResource(Res.string.workout_preview_start),
                onClick = { onIntent(WorkoutPreviewIntent.OnStartClicked) },
                size = LyteButtonSize.Large,
                icon = LyteIcons.Play,
                enabled = !content.isStarting,
                fullWidth = true,
            )
        }
    }
}

/** Удалённая программа — не сбой чтения: «не удалось загрузить» обещало бы, что она ещё вернётся. */
private fun LyteError.toMessageResource(): StringResource = when (this) {
    LyteError.NotFound -> Res.string.workout_preview_gone
    else -> Res.string.workout_preview_error
}

private fun LyteError.toHintResource(): StringResource? = when (this) {
    LyteError.NotFound -> Res.string.workout_preview_gone_hint
    else -> null
}

@Composable
@Preview
private fun WorkoutPreviewContentPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            WorkoutPreviewContent(
                state = WorkoutPreviewUiState.Content(program = previewProgram()),
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
                state = WorkoutPreviewUiState.Loading,
                onIntent = {},
            )
        }
    }
}

/** Программу не прочитать: сбой хранилища. Состав и кнопка старта не показываются вовсе. */
@Composable
@Preview
private fun WorkoutPreviewContentErrorPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            WorkoutPreviewContent(
                state = WorkoutPreviewUiState.Error(LyteError.Storage),
                onIntent = {},
            )
        }
    }
}

/**
 * Программу удалили, пока превью висело в стеке вкладки: тот же арм ошибки, но текст и подсказка
 * другие — сказать «не удалось загрузить» о том, чего больше нет, значит предложить подождать.
 */
@Composable
@Preview
private fun WorkoutPreviewContentGonePreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            WorkoutPreviewContent(
                state = WorkoutPreviewUiState.Error(LyteError.NotFound),
                onIntent = {},
            )
        }
    }
}

/** Старт нажат: пока пишется снапшот сессии, кнопка погашена — guard от дабл-тапа. */
@Composable
@Preview
private fun WorkoutPreviewContentStartingPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            WorkoutPreviewContent(
                state = WorkoutPreviewUiState.Content(program = previewProgram(), isStarting = true),
                onIntent = {},
            )
        }
    }
}

/**
 * Старт не удался: кнопка снова активна, над ней — строка ошибки на непрозрачной панели старта.
 * Состав программы баннер не накрывает, поэтому читаются обе части кадра.
 */
@Composable
@Preview
private fun WorkoutPreviewContentStartErrorPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            WorkoutPreviewContent(
                state = WorkoutPreviewUiState.Content(program = previewProgram(), startError = LyteError.Storage),
                onIntent = {},
            )
        }
    }
}

/** Кадр `preview-exercise`: шторка описания упражнения открыта поверх превью программы. */
@Composable
@Preview
private fun WorkoutPreviewContentExerciseInfoPreview() {
    val program = previewProgram()
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            WorkoutPreviewContent(
                state = WorkoutPreviewUiState.Content(
                    program = program,
                    exerciseInfo = program.exercises.first(),
                ),
                onIntent = {},
            )
        }
    }
}

private fun previewProgram(): WorkoutPreviewUiModel = WorkoutPreviewUiModel(
    programName = "Push Day",
    exerciseCount = 3,
    exercises = listOf(
        WorkoutPreviewExerciseUiModel(
            number = 1,
            name = "Жим лёжа",
            description = "Штанга, горизонтальная скамья",
            sets = listOf(
                LyteSetValue(reps = 8, weight = 70.0),
                LyteSetValue(reps = 8, weight = 80.0),
                LyteSetValue(reps = 6, weight = 85.0),
            ),
            accent = LyteAccent.Indigo,
            glyph = LyteExerciseGlyph.BenchPress,
        ),
        WorkoutPreviewExerciseUiModel(
            number = 2,
            name = "Жим гантелей на наклонной",
            description = "Угол 30°, гантели",
            sets = listOf(
                LyteSetValue(reps = 10, weight = 24.0),
                LyteSetValue(reps = 10, weight = 26.0),
                LyteSetValue(reps = 8, weight = 26.0),
            ),
            accent = LyteAccent.Teal,
            glyph = LyteExerciseGlyph.DumbbellPress,
        ),
        WorkoutPreviewExerciseUiModel(
            number = 3,
            name = "Отжимания на брусьях",
            description = null,
            sets = listOf(
                LyteSetValue(reps = 12),
                LyteSetValue(reps = 12),
                LyteSetValue(reps = 10),
            ),
            accent = LyteAccent.Amber,
            glyph = LyteExerciseGlyph.Rack,
        ),
    ),
)
