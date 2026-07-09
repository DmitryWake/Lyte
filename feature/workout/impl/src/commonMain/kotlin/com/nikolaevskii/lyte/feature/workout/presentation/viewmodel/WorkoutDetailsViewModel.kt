@file:OptIn(ExperimentalUuidApi::class)

package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseUiModel
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.toUiModel
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
            is WorkoutDetailsIntent.ChangeName -> updateState { copy(name = intent.name) }
            is WorkoutDetailsIntent.MoveExercise -> updateState { copy(exercises = exercises.moved(intent.fromIndex, intent.toIndex)) }
            is WorkoutDetailsIntent.RemoveExercise -> removeExercise(intent.index)
            // Пикер упражнений (3.3) — отдельная задача, пока кнопка ничего не делает.
            WorkoutDetailsIntent.AddExercise -> Unit
            is WorkoutDetailsIntent.EditExerciseSets -> updateState { copy(editingExerciseIndex = intent.index) }
            WorkoutDetailsIntent.CloseSetsEditor -> updateState { copy(editingExerciseIndex = null) }
            is WorkoutDetailsIntent.ChangeSetReps -> changeSetReps(intent.setIndex, intent.reps)
            is WorkoutDetailsIntent.ChangeSetWeight -> changeSetWeight(intent.setIndex, intent.weight)
            WorkoutDetailsIntent.AddSet -> addSet()
            is WorkoutDetailsIntent.RemoveSet -> removeSet(intent.setIndex)
            WorkoutDetailsIntent.Save -> save()
            WorkoutDetailsIntent.Back -> lyteNavigator.back()
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

    private fun addSet() {
        updateState { copy(exercises = updatingEditedSets { sets -> sets + sets.last() }) }
    }

    private fun removeSet(setIndex: Int) {
        updateState {
            copy(exercises = updatingEditedSets { sets -> if (sets.size > 1) sets.filterIndexed { i, _ -> i != setIndex } else sets })
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
}
