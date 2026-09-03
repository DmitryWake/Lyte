package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.feature.workout.WorkoutDetailsRoute
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutProgramUiModel
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutListViewModelTest {

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
    fun loadsWorkoutsOnInit() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(
            initialItems = listOf(
                program(
                    id = "w1",
                    name = "Push Day",
                    exerciseCount = 5,
                    accent = ExerciseAccent.Indigo,
                    glyph = ExerciseGlyph.BenchPress,
                ),
            ),
        )
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())

        runCurrent()

        assertEquals(
            WorkoutListUiState.Content(
                listOf(
                    uiModel(
                        id = "w1",
                        name = "Push Day",
                        exerciseCount = 5,
                        accent = LyteAccent.Indigo,
                        glyph = LyteExerciseGlyph.BenchPress,
                    ),
                ),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun listReactivelyUpdatesWhenRepositoryChanges() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        // Реактивный SSOT: изменение репозитория переэмитит поток — без ручного OnScreenShown.
        repository.items = listOf(program(id = "w1", name = "Push Day V2", exerciseCount = 4))
        runCurrent()

        assertEquals(
            WorkoutListUiState.Content(listOf(uiModel(id = "w1", name = "Push Day V2", exerciseCount = 4))),
            viewModel.uiState.value,
        )
    }

    @Test
    fun failedLoadShowsErrorState() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { getWorkoutsError = IllegalStateException("boom") }
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())

        runCurrent()

        assertTrue(viewModel.uiState.value is WorkoutListUiState.Error)
    }

    @Test
    fun requestDeleteShowsConfirmationWithoutDeleting() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))

        assertEquals("w1", (viewModel.uiState.value as WorkoutListUiState.Content).pendingDelete?.id)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun cancelDeleteClearsPendingDeleteWithoutDeleting() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()
        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))

        viewModel.onIntent(WorkoutListIntent.OnDeleteDismissed)

        assertNull((viewModel.uiState.value as WorkoutListUiState.Content).pendingDelete)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun confirmDeleteRemovesProgramAndListReprojects() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(
            initialItems = listOf(
                program(id = "w1", name = "Push Day", exerciseCount = 5),
                program(id = "w2", name = "Pull Day", exerciseCount = 4),
            ),
        )
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()
        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))

        viewModel.onIntent(WorkoutListIntent.OnDeleteConfirmed)
        runCurrent()

        assertEquals(listOf("w1"), repository.deletedIds)
        val content = viewModel.uiState.value as WorkoutListUiState.Content
        assertNull(content.pendingDelete)
        assertEquals(listOf("w2"), content.programs.map { it.id })
    }

    /**
     * Провал удаления: экран остаётся списком, диалог закрыт, о неудаче сообщает баннер. Без этого теста
     * ветку `onFailure` можно выкинуть целиком, и гейт останется зелёным.
     */
    @Test
    fun failedDeleteClosesDialogAndShowsActionError() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(
            initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)),
        ).apply { deleteWorkoutError = IllegalStateException("db down") }
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()
        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))

        viewModel.onIntent(WorkoutListIntent.OnDeleteConfirmed)
        runCurrent()

        val content = viewModel.uiState.value as WorkoutListUiState.Content
        assertNull(content.pendingDelete)
        assertIs<LyteError.Unknown>(content.actionError)
        // Программа на месте: удаление не прошло, список не обманывает.
        assertEquals(listOf("w1"), content.programs.map { it.id })
    }

    /** Повтор удаления гасит баннер прошлой неудачи: иначе во время попытки не понять, идёт ли она. */
    @Test
    fun retryingDeleteClearsPreviousActionError() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(
            initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)),
        ).apply { deleteWorkoutError = IllegalStateException("db down") }
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()
        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))
        viewModel.onIntent(WorkoutListIntent.OnDeleteConfirmed)
        runCurrent()
        assertNotNull((viewModel.uiState.value as WorkoutListUiState.Content).actionError)

        repository.deleteWorkoutError = null
        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))
        viewModel.onIntent(WorkoutListIntent.OnDeleteConfirmed)
        runCurrent()

        assertNull((viewModel.uiState.value as? WorkoutListUiState.Content)?.actionError)
    }

    @Test
    fun confirmDeleteWithoutPendingDoesNothing() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        viewModel.onIntent(WorkoutListIntent.OnDeleteConfirmed)
        runCurrent()

        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun openDetailsNavigatesToDetailsRouteWithProgramIdAndDoesNotChangeState() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val navigator = FakeLyteNavigator()
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        val stateBefore = viewModel.uiState.value

        viewModel.onIntent(WorkoutListIntent.OnProgramClicked(id = "w1"))

        assertEquals(listOf<Pair<Any, LyteNavOptions?>>(WorkoutDetailsRoute(id = "w1") to null), navigator.navigateCalls)
        assertEquals(stateBefore, viewModel.uiState.value)
    }

    @Test
    fun createProgramNavigatesToDetailsRouteWithNullId() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val navigator = FakeLyteNavigator()
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        val stateBefore = viewModel.uiState.value

        viewModel.onIntent(WorkoutListIntent.OnCreateProgramClicked)

        assertEquals(listOf<Pair<Any, LyteNavOptions?>>(WorkoutDetailsRoute(id = null) to null), navigator.navigateCalls)
        assertEquals(stateBefore, viewModel.uiState.value)
    }

    private fun program(
        id: String,
        name: String,
        exerciseCount: Int,
        accent: ExerciseAccent = ExerciseAccent.Default,
        glyph: ExerciseGlyph = ExerciseGlyph.Default,
    ): WorkoutItemEntity =
        WorkoutItemEntity(
            id = id,
            name = name,
            description = null,
            exerciseCount = exerciseCount,
            accent = accent,
            glyph = glyph,
        )

    private fun uiModel(
        id: String,
        name: String,
        exerciseCount: Int,
        accent: LyteAccent = LyteAccent.Default,
        glyph: LyteExerciseGlyph = LyteExerciseGlyph.Default,
    ): WorkoutProgramUiModel =
        WorkoutProgramUiModel(
            id = id,
            name = name,
            exerciseCount = exerciseCount,
            accent = accent,
            glyph = glyph,
        )
}
