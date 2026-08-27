package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutDetailsEditor
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseUiModel
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState.WorkoutDetailsContent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createModeStartsWithGeneratedIdBlankFormAndDoesNotLoad() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository()
        val viewModel = createViewModel(initialId = null, workoutRepository = repository)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.id.isNotBlank())
        assertEquals("", state.name)
        assertTrue(state.exercises.isEmpty())
        assertFalse(state.isLoading)
        assertTrue(repository.getWorkoutCalls.isEmpty())
    }

    @Test
    fun editModeLoadsWorkoutIntoState() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(
                id = "w1",
                name = "Push Day",
                description = "Chest & triceps",
                exercises = listOf(exercise("e1", "Bench Press", 8 to 70.0), exercise("e2", "Dips", 12 to null)),
            )
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)

        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("w1", state.id)
        assertEquals("Push Day", state.name)
        assertEquals("Chest & triceps", state.description)
        assertEquals(listOf("Bench Press", "Dips"), state.exercises.map { it.exercise.exercise.name })
        assertFalse(state.isLoading)
        assertEquals(listOf("w1"), repository.getWorkoutCalls)
    }

    @Test
    fun editModeLoadsProgramMarkIntoForm() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", accent = ExerciseAccent.Indigo, glyph = ExerciseGlyph.BenchPress)
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)

        runCurrent()

        assertEquals(ExerciseAccent.Indigo, viewModel.uiState.value.accent)
        assertEquals(ExerciseGlyph.BenchPress, viewModel.uiState.value.glyph)
    }

    @Test
    fun markClickOpensMarkSheetAndDismissClosesIt() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialId = null)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.OnMarkClicked)
        assertEquals(WorkoutDetailsEditor.Mark, viewModel.uiState.value.editor)

        viewModel.onIntent(WorkoutDetailsIntent.OnMarkSheetDismissed)
        assertNull(viewModel.uiState.value.editor)
    }

    /** Цвет и знак подбирают вместе, поэтому выбор не закрывает шторку — она остаётся открытой. */
    @Test
    fun changeAccentAndGlyphUpdatesDraftAndKeepsSheetOpen() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialId = null)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnMarkClicked)

        viewModel.onIntent(WorkoutDetailsIntent.OnAccentChanged(ExerciseAccent.Lime))
        viewModel.onIntent(WorkoutDetailsIntent.OnGlyphChanged(ExerciseGlyph.Squat))

        assertEquals(ExerciseAccent.Lime, viewModel.uiState.value.accent)
        assertEquals(ExerciseGlyph.Squat, viewModel.uiState.value.glyph)
        assertEquals(WorkoutDetailsEditor.Mark, viewModel.uiState.value.editor)
    }

    @Test
    fun editModeMissingWorkoutSurfacesErrorMessage() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialId = "missing", workoutRepository = FakeWorkoutRepository())

        runCurrent()

        assertTrue(viewModel.uiState.value.content is WorkoutDetailsContent.Error)
    }

    @Test
    fun editModeLoadFailureSurfacesErrorMessage() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { getWorkoutError = IllegalStateException("boom") }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)

        runCurrent()

        assertTrue(viewModel.uiState.value.content is WorkoutDetailsContent.Error)
    }

    @Test
    fun changeNameUpdatesState() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialId = null)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.OnNameChanged("Leg Day"))

        assertEquals("Leg Day", viewModel.uiState.value.name)
    }

    @Test
    fun moveExerciseReordersList() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(
                id = "w1",
                exercises = listOf(exercise("e1", "Squat", 8 to 90.0), exercise("e2", "Bench", 8 to 70.0), exercise("e3", "Row", 10 to 60.0)),
            )
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.OnExerciseMoved(fromIndex = 0, toIndex = 2))

        assertEquals(listOf("Bench", "Row", "Squat"), viewModel.uiState.value.exercises.map { it.exercise.exercise.name })
    }

    @Test
    fun moveExerciseWithOutOfBoundsIndexIsNoOp() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0), exercise("e2", "Bench", 8 to 70.0)))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.OnExerciseMoved(fromIndex = 5, toIndex = 0))

        assertEquals(listOf("Squat", "Bench"), viewModel.uiState.value.exercises.map { it.exercise.exercise.name })
    }

    @Test
    fun removeExerciseRemovesOnlyTargetedItem() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(
                id = "w1",
                exercises = listOf(exercise("e1", "Squat", 8 to 90.0), exercise("e2", "Bench", 8 to 70.0), exercise("e3", "Row", 10 to 60.0)),
            )
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.OnRemoveExerciseClicked(index = 1))

        assertEquals(listOf("Squat", "Row"), viewModel.uiState.value.exercises.map { it.exercise.exercise.name })
    }

    @Test
    fun addExerciseClickOpensPickerSheet() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialId = null)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.OnAddExerciseClicked)

        assertEquals(WorkoutDetailsEditor.ExercisePicker(), viewModel.uiState.value.editor)
    }

    @Test
    fun dismissingPickerClosesSheet() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialId = null)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnAddExerciseClicked)

        viewModel.onIntent(WorkoutDetailsIntent.OnExerciseSheetDismissed)

        assertNull(viewModel.uiState.value.editor)
    }

    @Test
    fun createExerciseClickReplacesPickerWithPrefilledCreator() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialId = null)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnAddExerciseClicked)

        viewModel.onIntent(WorkoutDetailsIntent.OnCreateExerciseClicked(query = "Жим Арнольда"))

        assertEquals(WorkoutDetailsEditor.ExerciseCreator(initialName = "Жим Арнольда"), viewModel.uiState.value.editor)
    }

    @Test
    fun dismissingCreatorReturnsToPickerWithSearchQuery() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialId = null)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnCreateExerciseClicked(query = "Жим Арнольда"))

        viewModel.onIntent(WorkoutDetailsIntent.OnExerciseSheetDismissed)

        assertEquals(WorkoutDetailsEditor.ExercisePicker(query = "Жим Арнольда"), viewModel.uiState.value.editor)
    }

    @Test
    fun addExerciseAppendsItWithoutSetsClosesSheetAndOpensSetsEditor() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0)))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnAddExerciseClicked)

        viewModel.onIntent(WorkoutDetailsIntent.OnExerciseSelected(WorkoutExerciseEntity(id = "l2", name = "Жим лёжа")))

        val state = viewModel.uiState.value
        assertEquals(listOf("Squat", "Жим лёжа"), state.exercises.map { it.exercise.exercise.name })
        assertTrue(state.exercises.last().exercise.reps.isEmpty())
        // Выбор закрывает шторку выбора и сразу открывает редактор подходов нового упражнения.
        assertEquals(state.exercises.lastIndex, state.editingExerciseIndex)
    }

    @Test
    fun editExerciseSetsOpensEditorForIndex() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(
                id = "w1",
                exercises = listOf(exercise("e1", "Squat", 8 to 90.0), exercise("e2", "Bench", 8 to 70.0)),
            )
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index = 1))

        assertEquals(1, viewModel.uiState.value.editingExerciseIndex)
    }

    @Test
    fun closeSetsEditorClearsEditingIndex() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0)))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index = 0))

        viewModel.onIntent(WorkoutDetailsIntent.OnSetsEditorDismissed)

        assertNull(viewModel.uiState.value.editingExerciseIndex)
    }

    @Test
    fun changeSetRepsUpdatesTargetedSetOnly() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0, 6 to 95.0)))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index = 0))

        viewModel.onIntent(WorkoutDetailsIntent.OnSetRepsChanged(setIndex = 1, reps = 5))

        val reps = viewModel.uiState.value.exercises.single().exercise.reps
        assertEquals(listOf(8 to 90.0, 5 to 95.0), reps.map { it.count to it.weight })
    }

    @Test
    fun changeSetWeightUpdatesTargetedSetOnly() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0, 6 to 95.0)))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index = 0))

        viewModel.onIntent(WorkoutDetailsIntent.OnSetWeightChanged(setIndex = 0, weight = 92.5))

        val reps = viewModel.uiState.value.exercises.single().exercise.reps
        assertEquals(listOf(8 to 92.5, 6 to 95.0), reps.map { it.count to it.weight })
    }

    @Test
    fun addSetAppendsCloneOfLastSet() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0, 6 to 95.0)))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index = 0))

        viewModel.onIntent(WorkoutDetailsIntent.OnAddSetClicked)

        val reps = viewModel.uiState.value.exercises.single().exercise.reps
        assertEquals(listOf(8 to 90.0, 6 to 95.0, 6 to 95.0), reps.map { it.count to it.weight })
    }

    @Test
    fun addSetOnExerciseWithoutSetsAppendsDefaultSet() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat")))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index = 0))

        viewModel.onIntent(WorkoutDetailsIntent.OnAddSetClicked)

        val reps = viewModel.uiState.value.exercises.single().exercise.reps
        assertEquals(listOf(8 to null), reps.map { it.count to it.weight })
    }

    @Test
    fun removeSetRemovesTargetedSet() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0, 6 to 95.0, 6 to 95.0)))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index = 0))

        viewModel.onIntent(WorkoutDetailsIntent.OnRemoveSetClicked(setIndex = 1))

        val reps = viewModel.uiState.value.exercises.single().exercise.reps
        assertEquals(listOf(8 to 90.0, 6 to 95.0), reps.map { it.count to it.weight })
    }

    /** Упражнение в программе без единого подхода бессмысленно, поэтому последний не удаляется. */
    @Test
    fun removeLastSetIsNoOp() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0)))
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnEditSetsClicked(index = 0))

        viewModel.onIntent(WorkoutDetailsIntent.OnRemoveSetClicked(setIndex = 0))

        val reps = viewModel.uiState.value.exercises.single().exercise.reps
        assertEquals(listOf(8 to 90.0), reps.map { it.count to it.weight })
    }

    @Test
    fun saveInCreateModeCallsCreateWorkoutAndNavigatesBack() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository()
        val navigator = FakeLyteNavigator()
        val viewModel = createViewModel(initialId = null, workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        val generatedId = viewModel.uiState.value.id
        viewModel.onIntent(WorkoutDetailsIntent.OnNameChanged("New Program"))

        viewModel.onIntent(WorkoutDetailsIntent.OnDoneClicked)
        runCurrent()

        assertEquals(listOf(WorkoutEntity(id = generatedId, name = "New Program", description = null, exercises = emptyList())), repository.createdWorkouts)
        assertTrue(repository.editedWorkouts.isEmpty())
        assertEquals(1, navigator.backCallCount)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun saveInEditModePreservesDescriptionAndCallsEditWorkout() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", name = "Push Day", description = "Original description", exercises = listOf(exercise("e1", "Squat", 8 to 90.0)))
        }
        val navigator = FakeLyteNavigator()
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnNameChanged("Renamed"))

        viewModel.onIntent(WorkoutDetailsIntent.OnDoneClicked)
        runCurrent()

        val saved = repository.editedWorkouts.single()
        assertEquals("w1", saved.id)
        assertEquals("Renamed", saved.name)
        assertEquals("Original description", saved.description)
        assertTrue(repository.createdWorkouts.isEmpty())
        assertEquals(1, navigator.backCallCount)
    }

    /**
     * Регрессия: у `accent`/`glyph` в [WorkoutEntity] есть значения по умолчанию, поэтому пропуск
     * полей при сборке сущности не ломал компиляцию, а молча перекрашивал программу в slate/squat
     * при каждом сохранении.
     */
    @Test
    fun saveInEditModeKeepsProgramMarkUntouched() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", accent = ExerciseAccent.Coral, glyph = ExerciseGlyph.PullUp)
        }
        val viewModel = createViewModel(initialId = "w1", workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnNameChanged("Renamed"))

        viewModel.onIntent(WorkoutDetailsIntent.OnDoneClicked)
        runCurrent()

        val saved = repository.editedWorkouts.single()
        assertEquals(ExerciseAccent.Coral, saved.accent)
        assertEquals(ExerciseGlyph.PullUp, saved.glyph)
    }

    @Test
    fun saveInCreateModeWritesChosenMark() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository()
        val viewModel = createViewModel(initialId = null, workoutRepository = repository)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnNameChanged("Leg Day"))
        viewModel.onIntent(WorkoutDetailsIntent.OnAccentChanged(ExerciseAccent.Lime))
        viewModel.onIntent(WorkoutDetailsIntent.OnGlyphChanged(ExerciseGlyph.Squat))

        viewModel.onIntent(WorkoutDetailsIntent.OnDoneClicked)
        runCurrent()

        val created = repository.createdWorkouts.single()
        assertEquals(ExerciseAccent.Lime, created.accent)
        assertEquals(ExerciseGlyph.Squat, created.glyph)
    }

    @Test
    fun saveFailureKeepsFormAndSurfacesErrorWithoutNavigating() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { createWorkoutError = IllegalStateException("save failed") }
        val navigator = FakeLyteNavigator()
        val viewModel = createViewModel(initialId = null, workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.OnNameChanged("New Program"))

        viewModel.onIntent(WorkoutDetailsIntent.OnDoneClicked)
        runCurrent()

        assertNotNull(viewModel.uiState.value.saveError)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals("New Program", viewModel.uiState.value.name)
        assertEquals(0, navigator.backCallCount)
    }

    @Test
    fun backCallsNavigatorBackWithoutTouchingRepository() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository()
        val navigator = FakeLyteNavigator()
        val viewModel = createViewModel(initialId = null, workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.OnBackClicked)

        assertEquals(1, navigator.backCallCount)
        assertTrue(repository.createdWorkouts.isEmpty())
        assertTrue(repository.editedWorkouts.isEmpty())
    }

    private fun createViewModel(
        initialId: String?,
        workoutRepository: FakeWorkoutRepository = FakeWorkoutRepository(),
        lyteNavigator: FakeLyteNavigator = FakeLyteNavigator(),
    ): WorkoutDetailsViewModel = WorkoutDetailsViewModel(
        initialId = initialId,
        workoutRepository = workoutRepository,
        lyteNavigator = lyteNavigator,
    )

    private fun workout(
        id: String,
        name: String = "Push Day",
        description: String? = null,
        accent: ExerciseAccent = ExerciseAccent.Default,
        glyph: ExerciseGlyph = ExerciseGlyph.Default,
        exercises: List<WorkoutExerciseWithRepsEntity> = emptyList(),
    ): WorkoutEntity = WorkoutEntity(
        id = id,
        name = name,
        description = description,
        accent = accent,
        glyph = glyph,
        exercises = exercises,
    )

    private fun exercise(id: String, name: String, vararg plan: Pair<Int, Double?>): WorkoutExerciseWithRepsEntity =
        WorkoutExerciseWithRepsEntity(
            exercise = WorkoutExerciseEntity(id = id, name = name),
            reps = plan.map { (count, weight) -> WorkoutRepEntity(count = count, weight = weight) },
        )
}

private val WorkoutDetailsUiState.editingArm: WorkoutDetailsContent.Editing?
    get() = content as? WorkoutDetailsContent.Editing
private val WorkoutDetailsUiState.name: String? get() = editingArm?.name
private val WorkoutDetailsUiState.description: String? get() = editingArm?.description
private val WorkoutDetailsUiState.accent: ExerciseAccent? get() = editingArm?.accent
private val WorkoutDetailsUiState.glyph: ExerciseGlyph? get() = editingArm?.glyph
private val WorkoutDetailsUiState.exercises: List<WorkoutExerciseUiModel> get() = editingArm?.exercises.orEmpty()
private val WorkoutDetailsUiState.editor: WorkoutDetailsEditor? get() = editingArm?.editor
private val WorkoutDetailsUiState.isSaving: Boolean get() = editingArm?.isSaving == true
private val WorkoutDetailsUiState.isLoading: Boolean get() = content is WorkoutDetailsContent.Loading
private val WorkoutDetailsUiState.saveError get() = editingArm?.saveError
private val WorkoutDetailsUiState.editingExerciseIndex: Int?
    get() = (editingArm?.editor as? WorkoutDetailsEditor.SetsEditor)?.exerciseIndex
