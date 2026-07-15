package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.format.formatWeight
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.component.card.LyteExerciseCard
import com.nikolaevskii.lyte.core.design.component.card.LyteExerciseCardVariant
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.textfield.LyteTextField
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_add_exercise
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_error
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercises_title
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_name_label
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_save
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_save_error
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_set_bodyweight
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_set_weight
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_title
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutDetailsEditor
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState.WorkoutDetailsContent
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseUiModel
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutDetailsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val NAME_FIELD_KEY = "name-field"
private const val EXERCISES_OVERLINE_KEY = "exercises-overline"
private const val ADD_EXERCISE_BUTTON_KEY = "add-exercise-button"

@Composable
fun WorkoutDetailsScreen(
    id: String?,
    viewModel: WorkoutDetailsViewModel = koinViewModel { parametersOf(id) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutDetailsContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun WorkoutDetailsContent(
    state: WorkoutDetailsUiState,
    onIntent: (WorkoutDetailsIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            LyteTopBar(
                title = stringResource(Res.string.workout_details_title),
                onBack = { onIntent(WorkoutDetailsIntent.OnBackClicked) },
            )
        },
        bottomBar = {
            val editing = state.content as? WorkoutDetailsContent.Editing
            if (editing != null) {
                WorkoutDetailsSaveBar(
                    isSaving = editing.isSaving,
                    onSave = { onIntent(WorkoutDetailsIntent.OnSaveClicked) },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val content = state.content) {
                WorkoutDetailsContent.Loading ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                is WorkoutDetailsContent.Error ->
                    Text(
                        text = stringResource(Res.string.workout_details_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s3),
                    )

                is WorkoutDetailsContent.Editing -> {
                    // Ошибка записи — баннер НАД формой; форму (введённые данные) не стирает.
                    if (content.saveError != null) {
                        Text(
                            text = stringResource(Res.string.workout_details_save_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s3),
                        )
                    }
                    WorkoutDetailsForm(
                        editing = content,
                        onIntent = onIntent,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Оверлеи (шторки/редактор подходов) ведут свои ViewModel и отдают наверх только события.
        when (val editor = (state.content as? WorkoutDetailsContent.Editing)?.editor) {
            is WorkoutDetailsEditor.ExercisePicker -> WorkoutExercisePickerSheet(
                onExercisePicked = { exercise -> onIntent(WorkoutDetailsIntent.OnExerciseSelected(exercise)) },
                onCreateExerciseRequested = { query -> onIntent(WorkoutDetailsIntent.OnCreateExerciseClicked(query)) },
                onDismissRequest = { onIntent(WorkoutDetailsIntent.OnExerciseSheetDismissed) },
                initialQuery = editor.query,
            )

            is WorkoutDetailsEditor.ExerciseCreator -> WorkoutExerciseCreateSheet(
                initialName = editor.initialName,
                onExerciseCreated = { exercise -> onIntent(WorkoutDetailsIntent.OnExerciseSelected(exercise)) },
                onDismissRequest = { onIntent(WorkoutDetailsIntent.OnExerciseSheetDismissed) },
            )

            is WorkoutDetailsEditor.SetsEditor -> {
                val exercise = (state.content as? WorkoutDetailsContent.Editing)?.exercises
                    ?.getOrNull(editor.exerciseIndex)?.exercise
                if (exercise != null) {
                    WorkoutSetsEditorSheet(exercise = exercise, onIntent = onIntent)
                }
            }

            null -> Unit
        }
    }
}

@Composable
private fun WorkoutDetailsSaveBar(
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = LyteTheme.elevation.level2,
        modifier = modifier.fillMaxWidth(),
    ) {
        LyteButton(
            text = stringResource(Res.string.workout_details_save),
            onClick = onSave,
            enabled = !isSaving,
            fullWidth = true,
            modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s4),
        )
    }
}

/**
 * Список упражнений умеет менять порядок drag-хэндлом: [detectDragGestures] висит только на хэндле
 * (см. [LyteExerciseCard.dragHandleModifier]), поэтому обычный скролл списка им не перехватывается.
 * Перетаскиваемый элемент двигается вручную через `graphicsLayer` по позициям из
 * `listState.layoutInfo`, остальные — доезжают в новое место сами через `Modifier.animateItem()`.
 * [rememberUpdatedState] нужен, т.к. корутина жеста не пересоздаётся на каждую рекомпозицию (ключ —
 * стабильный [WorkoutExerciseUiModel.key]), поэтому без него `onDrag` видел бы список на момент
 * начала жеста, а не текущий — второй своп подряд считался бы по устаревшим индексам.
 */
@Composable
private fun WorkoutDetailsForm(
    editing: WorkoutDetailsContent.Editing,
    onIntent: (WorkoutDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val currentExercises by rememberUpdatedState(editing.exercises)
    val currentOnIntent by rememberUpdatedState(onIntent)
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s1),
        verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s3),
        modifier = modifier.fillMaxWidth(),
    ) {
        item(key = NAME_FIELD_KEY) {
            LyteTextField(
                value = editing.name,
                onValueChange = { name -> onIntent(WorkoutDetailsIntent.OnNameChanged(name)) },
                label = stringResource(Res.string.workout_details_name_label),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item(key = EXERCISES_OVERLINE_KEY) {
            LyteOverline(text = stringResource(Res.string.workout_details_exercises_title))
        }
        itemsIndexed(items = editing.exercises, key = { _, item -> item.key }) { index, item ->
            val isDragging = item.key == draggingKey
            LyteExerciseCard(
                title = item.exercise.exercise.name,
                setLabels = item.exercise.reps.map { rep -> formatSetLabel(rep) },
                variant = LyteExerciseCardVariant.Editor(
                    onEdit = { onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index)) },
                    onRemove = { onIntent(WorkoutDetailsIntent.OnRemoveExerciseClicked(index)) },
                    dragHandleModifier = Modifier.pointerInput(item.key) {
                        detectDragGestures(
                            onDragStart = {
                                draggingKey = item.key
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                draggingKey = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggingKey = null
                                dragOffset = 0f
                            },
                            onDrag = { change, delta ->
                                change.consume()
                                dragOffset += delta.y

                                val draggedInfo = listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { info -> info.key == item.key }
                                    ?: return@detectDragGestures
                                val exerciseKeys = currentExercises.map { exercise -> exercise.key }.toSet()
                                val draggedCenter = draggedInfo.offset + draggedInfo.size / 2f + dragOffset
                                val targetInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { candidate ->
                                    candidate.key != item.key &&
                                        candidate.key in exerciseKeys &&
                                        draggedCenter >= candidate.offset &&
                                        draggedCenter <= candidate.offset + candidate.size
                                } ?: return@detectDragGestures

                                val fromIndex = currentExercises.indexOfFirst { exercise -> exercise.key == item.key }
                                val toIndex = currentExercises.indexOfFirst { exercise -> exercise.key == targetInfo.key }
                                if (fromIndex != -1 && toIndex != -1) {
                                    dragOffset += draggedInfo.offset - targetInfo.offset
                                    currentOnIntent(WorkoutDetailsIntent.OnExerciseMoved(fromIndex, toIndex))
                                }
                            },
                        )
                    },
                ),
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = if (isDragging) dragOffset else 0f }
                    .let { base -> if (isDragging) base else base.animateItem() },
            )
        }
        item(key = ADD_EXERCISE_BUTTON_KEY) {
            LyteButton(
                text = stringResource(Res.string.workout_details_add_exercise),
                onClick = { onIntent(WorkoutDetailsIntent.OnAddExerciseClicked) },
                variant = LyteButtonVariant.Tonal,
                size = LyteButtonSize.Small,
                icon = LyteIcons.Plus,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun formatSetLabel(rep: WorkoutRepEntity): String {
    val weight = rep.weight
    return if (weight != null && weight > 0.0) {
        stringResource(Res.string.workout_details_set_weight, rep.count, formatWeight(weight))
    } else {
        stringResource(Res.string.workout_details_set_bodyweight, rep.count)
    }
}


@Composable
@Preview
private fun WorkoutDetailsContentPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(
                id = "1",
                content = WorkoutDetailsContent.Editing(
                    name = "Push Day",
                    description = null,
                    exercises = listOf(
                        previewExercise(key = "1", name = "Жим лёжа", plan = listOf(8 to 70.0, 8 to 80.0, 6 to 85.0)),
                        previewExercise(key = "2", name = "Жим гантелей на наклонной", plan = listOf(10 to 24.0, 10 to 26.0, 8 to 26.0)),
                        previewExercise(key = "3", name = "Отжимания на брусьях", plan = listOf(12 to null, 12 to null, 10 to null)),
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun WorkoutDetailsContentLoadingPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(id = "1", content = WorkoutDetailsContent.Loading),
            onIntent = {},
        )
    }
}

private fun previewExercise(key: String, name: String, plan: List<Pair<Int, Double?>>): WorkoutExerciseUiModel =
    WorkoutExerciseUiModel(
        key = key,
        exercise = WorkoutExerciseWithRepsEntity(
            exercise = WorkoutExerciseEntity(id = key, name = name),
            reps = plan.map { (count, weight) -> WorkoutRepEntity(count = count, weight = weight) },
        ),
    )
