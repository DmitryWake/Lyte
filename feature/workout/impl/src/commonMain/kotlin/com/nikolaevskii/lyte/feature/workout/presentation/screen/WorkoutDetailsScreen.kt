package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.component.card.LyteExerciseCard
import com.nikolaevskii.lyte.core.design.component.card.LyteExerciseCardVariant
import com.nikolaevskii.lyte.core.design.component.mark.LyteExerciseMark
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.textfield.LyteTextField
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_exercise_count
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_add_exercise
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_done
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_error
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercises_empty
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_exercises_title
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_mark_a11y
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_name_label
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_name_placeholder
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_save_error
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_set_count
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_title
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutDetailsEditor
import com.nikolaevskii.lyte.feature.workout.presentation.model.toLyteAccent
import com.nikolaevskii.lyte.feature.workout.presentation.model.toLyteGlyph
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState.WorkoutDetailsContent
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseUiModel
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutDetailsViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val HEADER_KEY = "header"
private const val EXERCISES_OVERLINE_KEY = "exercises-overline"
private const val EXERCISES_EMPTY_KEY = "exercises-empty"
private const val ADD_EXERCISE_BUTTON_KEY = "add-exercise-button"

private val HeaderMarkSize = 52.dp

/** Ряд выровнен по нижнему краю, а маркер приподнят над низом поля — `padding-bottom:4px` макета. */
private val HeaderMarkBottomPadding = 4.dp

private val EmptyExercisesPadding = 18.dp
private val DoneBarPaddingBottom = 28.dp

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
    // imePadding: приложение edge-to-edge (`enableEdgeToEdge()` в MainActivity), поэтому окно
    // клавиатурой не ужимается — без него IME, поднятая полем «Название», перекрыла бы прибитую
    // к низу кнопку «Готово». Тот же приём, что в `LyteBottomSheet`.
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            LyteTopBar(
                title = stringResource(Res.string.workout_details_title),
                size = LyteTopBarSize.Large,
                onBack = { onIntent(WorkoutDetailsIntent.OnBackClicked) },
            )
        },
        bottomBar = {
            val editing = state.content as? WorkoutDetailsContent.Editing
            if (editing != null) {
                WorkoutDetailsDoneBar(
                    isSaving = editing.isSaving,
                    onDone = { onIntent(WorkoutDetailsIntent.OnDoneClicked) },
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
        val editing = state.content as? WorkoutDetailsContent.Editing
        if (editing != null) {
            when (val editor = editing.editor) {
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
                    val exercise = editing.exercises.getOrNull(editor.exerciseIndex)?.exercise
                    if (exercise != null) {
                        WorkoutSetsEditorSheet(exercise = exercise, onIntent = onIntent)
                    }
                }

                WorkoutDetailsEditor.Mark -> WorkoutProgramMarkSheet(
                    accent = editing.accent,
                    glyph = editing.glyph,
                    onIntent = onIntent,
                )

                null -> Unit
            }
        }
    }
}

@Composable
private fun WorkoutDetailsDoneBar(
    isSaving: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = LyteTheme.elevation.level2,
        modifier = modifier.fillMaxWidth(),
    ) {
        LyteButton(
            text = stringResource(Res.string.workout_details_done),
            onClick = onDone,
            enabled = !isSaving,
            fullWidth = true,
            modifier = Modifier.padding(
                start = LyteTheme.spacing.s5,
                end = LyteTheme.spacing.s5,
                top = LyteTheme.spacing.s3,
                bottom = DoneBarPaddingBottom,
            ),
        )
    }
}

/**
 * Шапка формы: маркер программы и её название в одну строку. Маркер кликабельный — открывает шторку
 * «Цвет и знак»; отдельной кнопки у него в макете нет, потому что сам маркер и есть превью выбора.
 */
@Composable
private fun WorkoutDetailsHeader(
    name: String,
    accent: ExerciseAccent,
    glyph: ExerciseGlyph,
    onIntent: (WorkoutDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s3),
        modifier = modifier.fillMaxWidth(),
    ) {
        LyteExerciseMark(
            accent = accent.toLyteAccent(),
            glyph = glyph.toLyteGlyph(),
            size = HeaderMarkSize,
            contentDescription = stringResource(Res.string.workout_details_mark_a11y),
            modifier = Modifier
                .padding(bottom = HeaderMarkBottomPadding)
                .clip(CircleShape)
                .clickable(role = Role.Button) { onIntent(WorkoutDetailsIntent.OnMarkClicked) },
        )
        LyteTextField(
            value = name,
            onValueChange = { value -> onIntent(WorkoutDetailsIntent.OnNameChanged(value)) },
            label = stringResource(Res.string.workout_details_name_label),
            placeholder = stringResource(Res.string.workout_details_name_placeholder),
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Пустой состав — плашка с подсказкой, а не `LyteEmptyState`: у экрана уже есть и заголовок, и
 * действие «Добавить упражнение», поэтому полноэкранное пустое состояние повторило бы их третий раз.
 */
@Composable
private fun WorkoutDetailsEmptyExercises(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.workout_details_exercises_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(EmptyExercisesPadding),
    )
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
        item(key = HEADER_KEY) {
            WorkoutDetailsHeader(
                name = editing.name,
                accent = editing.accent,
                glyph = editing.glyph,
                onIntent = onIntent,
            )
        }
        item(key = EXERCISES_OVERLINE_KEY) {
            val exerciseCount = editing.exercises.size
            LyteOverline(
                text = if (exerciseCount > 0) {
                    pluralStringResource(Res.plurals.workout_exercise_count, exerciseCount, exerciseCount)
                } else {
                    stringResource(Res.string.workout_details_exercises_title)
                },
            )
        }
        if (editing.exercises.isEmpty()) {
            item(key = EXERCISES_EMPTY_KEY) {
                WorkoutDetailsEmptyExercises()
            }
        }
        itemsIndexed(items = editing.exercises, key = { _, item -> item.key }) { index, item ->
            val isDragging = item.key == draggingKey
            val setCount = item.exercise.reps.size
            LyteExerciseCard(
                title = item.exercise.exercise.name,
                accent = item.exercise.exercise.accent.toLyteAccent(),
                glyph = item.exercise.exercise.glyph.toLyteGlyph(),
                setCount = setCount,
                setsLabel = pluralStringResource(Res.plurals.workout_details_set_count, setCount, setCount),
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
                // Тап по карточке ведёт в подходы — как в макете; кнопка edit остаётся для явности.
                onClick = { onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index)) },
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
                icon = LyteIcons.Plus,
                fullWidth = true,
            )
        }
    }
}

@Composable
@Preview
private fun WorkoutDetailsContentPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(id = "1", content = previewEditing()),
            onIntent = {},
        )
    }
}

/** Новая программа: пустой состав — плашка вместо списка, название ещё не введено. */
@Composable
@Preview
private fun WorkoutDetailsContentEmptyPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(
                id = "1",
                content = WorkoutDetailsContent.Editing(
                    name = "",
                    description = null,
                    accent = ExerciseAccent.Default,
                    glyph = ExerciseGlyph.Default,
                    exercises = emptyList(),
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

@Composable
@Preview
private fun WorkoutDetailsContentErrorPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(id = "1", content = WorkoutDetailsContent.Error(LyteError.Storage)),
            onIntent = {},
        )
    }
}

/** Идёт запись программы: «Готово» погашена, форма остаётся видимой и заполненной. */
@Composable
@Preview
private fun WorkoutDetailsContentSavingPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(id = "1", content = previewEditing(isSaving = true)),
            onIntent = {},
        )
    }
}

/** Запись не прошла: баннер над формой, введённое не стёрто. */
@Composable
@Preview
private fun WorkoutDetailsContentSaveErrorPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(id = "1", content = previewEditing(saveError = LyteError.Storage)),
            onIntent = {},
        )
    }
}

/**
 * Подарм `editor = SetsEditor`: редактор подходов первого упражнения поверх формы.
 *
 * Экранных кадров у подармов `ExercisePicker` и `ExerciseCreator` нет намеренно: обе шторки поднимают
 * свою ViewModel через `koinViewModel()`, а скриншот-тест Koin не поднимает — такой кадр не «плохо
 * выглядел бы», а уронил бы гейт. Обе шторки полноэкранные, экран за ними не виден, поэтому их армы
 * сняты превью собственного stateless-контента в их же файлах.
 */
@Composable
@Preview
private fun WorkoutDetailsContentSetsEditorPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(
                id = "1",
                content = previewEditing(editor = WorkoutDetailsEditor.SetsEditor(exerciseIndex = 0)),
            ),
            onIntent = {},
        )
    }
}

/** Подарм `editor = Mark`: шторка «Цвет и знак» поверх формы (кадр `program-mark`). */
@Composable
@Preview
private fun WorkoutDetailsContentMarkSheetPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(id = "1", content = previewEditing(editor = WorkoutDetailsEditor.Mark)),
            onIntent = {},
        )
    }
}

/**
 * Та же программа, что в основном превью: новые кадры отличаются состоянием, а не данными, — иначе
 * дифф пришлось бы читать через различия в составе.
 */
private fun previewEditing(
    editor: WorkoutDetailsEditor? = null,
    isSaving: Boolean = false,
    saveError: LyteError? = null,
): WorkoutDetailsContent.Editing = WorkoutDetailsContent.Editing(
    name = "Push Day",
    description = null,
    accent = ExerciseAccent.Indigo,
    glyph = ExerciseGlyph.BenchPress,
    exercises = listOf(
        previewExercise(
            key = "1",
            name = "Жим лёжа",
            accent = ExerciseAccent.Indigo,
            glyph = ExerciseGlyph.BenchPress,
            plan = listOf(8 to 70.0, 8 to 80.0, 6 to 85.0),
        ),
        previewExercise(
            key = "2",
            name = "Жим гантелей на наклонной",
            accent = ExerciseAccent.Teal,
            glyph = ExerciseGlyph.DumbbellPress,
            plan = listOf(10 to 24.0, 10 to 26.0, 8 to 26.0),
        ),
        previewExercise(
            key = "3",
            name = "Отжимания на брусьях",
            accent = ExerciseAccent.Amber,
            glyph = ExerciseGlyph.Rack,
            plan = listOf(12 to null, 12 to null, 10 to null),
        ),
    ),
    editor = editor,
    isSaving = isSaving,
    saveError = saveError,
)

private fun previewExercise(
    key: String,
    name: String,
    accent: ExerciseAccent,
    glyph: ExerciseGlyph,
    plan: List<Pair<Int, Double?>>,
): WorkoutExerciseUiModel =
    WorkoutExerciseUiModel(
        key = key,
        exercise = WorkoutExerciseWithRepsEntity(
            exercise = WorkoutExerciseEntity(id = key, name = name, accent = accent, glyph = glyph),
            reps = plan.map { (count, weight) -> WorkoutRepEntity(count = count, weight = weight) },
        ),
    )
