@file:OptIn(ExperimentalUuidApi::class)

package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseSheet
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseUiModel
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.toUiModel
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Редактор программы. Библиотекой упражнений не занимается: её загрузка, поиск и создание живут в
 * своих ViewModel-ях внутри шторок, а сюда через [WorkoutDetailsIntent.OnExerciseSelected] приходит уже
 * готовое упражнение.
 */
class WorkoutDetailsViewModel(
    private val initialId: String?,
    private val workoutRepository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutDetailsUiState, WorkoutDetailsIntent>() {

    init {
        initialId?.let { id -> launch { loadWorkout(id) } }
    }

    override fun onIntent(intent: WorkoutDetailsIntent) {
        when (intent) {
            is WorkoutDetailsIntent.OnNameChanged -> updateState { copy(name = intent.name) }
            is WorkoutDetailsIntent.OnExerciseMoved -> updateState { copy(exercises = exercises.moved(intent.fromIndex, intent.toIndex)) }
            is WorkoutDetailsIntent.OnRemoveExerciseClicked -> removeExercise(intent.index)
            WorkoutDetailsIntent.OnAddExerciseClicked -> updateState { copy(exerciseSheet = WorkoutExerciseSheet.Picker()) }
            WorkoutDetailsIntent.OnExerciseSheetDismissed -> dismissExerciseSheet()
            is WorkoutDetailsIntent.OnCreateExerciseClicked ->
                updateState { copy(exerciseSheet = WorkoutExerciseSheet.Creator(initialName = intent.query)) }
            is WorkoutDetailsIntent.OnExerciseSelected -> addExercise(intent.exercise)
            is WorkoutDetailsIntent.OnEditSetsClicked -> updateState { copy(editingExerciseIndex = intent.index) }
            WorkoutDetailsIntent.OnSetsEditorDismissed -> updateState { copy(editingExerciseIndex = null) }
            is WorkoutDetailsIntent.OnSetRepsChanged -> changeSetReps(intent.setIndex, intent.reps)
            is WorkoutDetailsIntent.OnSetWeightChanged -> changeSetWeight(intent.setIndex, intent.weight)
            WorkoutDetailsIntent.OnAddSetClicked -> addSet()
            is WorkoutDetailsIntent.OnRemoveSetClicked -> removeSet(intent.setIndex)
            WorkoutDetailsIntent.OnSaveClicked -> save()
            WorkoutDetailsIntent.OnBackClicked -> lyteNavigator.back()
        }
    }

    override fun getInitialState(): WorkoutDetailsUiState = WorkoutDetailsUiState(id = initialId ?: Uuid.random().toString())

    private suspend fun loadWorkout(id: String) {
        updateState { copy(isLoading = true, errorMessage = null) }
        runCatching { checkNotNull(workoutRepository.getWorkout(id)) { "Workout $id not found" } }
            .onSuccess { workout ->
                updateState {
                    copy(
                        isLoading = false,
                        name = workout.name,
                        description = workout.description,
                        exercises = workout.exercises.map { it.toUiModel() },
                    )
                }
            }
            .onFailure { error -> updateState { copy(isLoading = false, errorMessage = error.message) } }
    }

    private fun save() {
        val state = uiStateValue
        launch {
            updateState { copy(isSaving = true, errorMessage = null) }
            val workoutEntity = WorkoutEntity(
                id = state.id,
                name = state.name,
                description = state.description,
                exercises = state.exercises.map { it.exercise },
            )
            runCatching {
                if (initialId == null) {
                    workoutRepository.createWorkout(workoutEntity)
                } else {
                    workoutRepository.editWorkout(workoutEntity)
                }
            }
                .onSuccess {
                    updateState { copy(isSaving = false) }
                    lyteNavigator.back()
                }
                .onFailure { error -> updateState { copy(isSaving = false, errorMessage = error.message) } }
        }
    }

    private fun removeExercise(index: Int) {
        updateState { copy(exercises = exercises.filterIndexed { i, _ -> i != index }) }
    }

    /**
     * Закрытие формы создания — это возврат к шторке выбора, а не выход: пользователь передумал
     * создавать упражнение, но не искать его. Поисковый запрос возвращаем тот же, с которым ушли,
     * потому что шторка выбора пересоздаётся вместе со своей ViewModel.
     */
    private fun dismissExerciseSheet() {
        updateState {
            val sheet = exerciseSheet
            copy(
                exerciseSheet = when (sheet) {
                    is WorkoutExerciseSheet.Creator -> WorkoutExerciseSheet.Picker(query = sheet.initialName)
                    is WorkoutExerciseSheet.Picker, null -> null
                },
            )
        }
    }

    /**
     * Упражнение добавляется без подходов, поэтому шторка закрывается сразу в редактор подходов —
     * иначе упражнение осталось бы в программе с пустым планом.
     */
    private fun addExercise(exercise: WorkoutExerciseEntity) {
        updateState {
            val updatedExercises = exercises +
                WorkoutExerciseWithRepsEntity(exercise = exercise, reps = emptyList()).toUiModel()
            copy(
                exercises = updatedExercises,
                exerciseSheet = null,
                editingExerciseIndex = updatedExercises.lastIndex,
            )
        }
    }

    private fun changeSetReps(setIndex: Int, reps: Int) {
        updateState {
            copy(exercises = updatingEditedSets { sets -> sets.mapIndexed { i, set -> if (i == setIndex) set.copy(count = reps) else set } })
        }
    }

    private fun changeSetWeight(setIndex: Int, weight: Double) {
        updateState {
            copy(exercises = updatingEditedSets { sets -> sets.mapIndexed { i, set -> if (i == setIndex) set.copy(weight = weight) else set } })
        }
    }

    /** Новый подход копирует предыдущий; у только что выбранного из библиотеки упражнения копировать нечего. */
    private fun addSet() {
        updateState { copy(exercises = updatingEditedSets { sets -> sets + (sets.lastOrNull() ?: DEFAULT_REP) }) }
    }

    private fun removeSet(setIndex: Int) {
        updateState {
            copy(exercises = updatingEditedSets { sets -> sets.filterIndexed { i, _ -> i != setIndex } })
        }
    }

    private fun List<WorkoutExerciseUiModel>.moved(fromIndex: Int, toIndex: Int): List<WorkoutExerciseUiModel> {
        if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) return this
        return toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    private fun WorkoutDetailsUiState.updatingEditedSets(
        transform: (List<WorkoutRepEntity>) -> List<WorkoutRepEntity>,
    ): List<WorkoutExerciseUiModel> {
        val index = editingExerciseIndex ?: return exercises
        return exercises.mapIndexed { i, model ->
            if (i == index) model.copy(exercise = model.exercise.copy(reps = transform(model.exercise.reps))) else model
        }
    }

    private companion object {
        /** Подход по умолчанию: 8 повторений со своим весом — вес пользователь проставит сам. */
        val DEFAULT_REP = WorkoutRepEntity(count = 8, weight = null)
    }
}
