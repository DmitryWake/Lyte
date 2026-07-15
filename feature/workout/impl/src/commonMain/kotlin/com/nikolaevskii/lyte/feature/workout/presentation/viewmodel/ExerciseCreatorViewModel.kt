@file:OptIn(ExperimentalUuidApi::class)

package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutExerciseRepository
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorUiState
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Форма создания упражнения библиотеки. [initialName] приходит из поискового запроса шторки выбора.
 *
 * `id` упражнения выдаётся сразу, при открытии формы, поэтому по успешной записи владельцу отдаётся
 * та же модель, что легла в библиотеку — см. [ExerciseCreatorUiState.isCreated].
 */
class ExerciseCreatorViewModel(
    private val initialName: String,
    private val workoutExerciseRepository: WorkoutExerciseRepository,
) : BaseViewModel<ExerciseCreatorUiState, ExerciseCreatorIntent>() {

    override fun onIntent(intent: ExerciseCreatorIntent) {
        when (intent) {
            is ExerciseCreatorIntent.OnNameChanged ->
                updateState { copy(exercise = exercise.copy(name = intent.name)).withSubmitEnabled() }

            is ExerciseCreatorIntent.OnDescriptionChanged ->
                updateState { copy(exercise = exercise.copy(description = intent.description)) }

            ExerciseCreatorIntent.OnCreateClicked -> submit()
        }
    }

    override fun getInitialState(): ExerciseCreatorUiState =
        ExerciseCreatorUiState(
            exercise = WorkoutExerciseEntity(id = Uuid.random().toString(), name = initialName),
        ).withSubmitEnabled()

    private fun submit() {
        if (!uiStateValue.isSubmitEnabled) {
            return
        }
        // Нормализованное упражнение кладём в состояние: наружу должно уйти ровно то, что записано.
        val exercise = uiStateValue.exercise.normalized()
        // isSaving поднимаем до launch, иначе второй тап успел бы пройти guard до старта корутины.
        updateState {
            copy(
                exercise = exercise,
                isSaving = true,
                errorMessage = null
            ).withSubmitEnabled()
        }
        launch {
            runCatching { workoutExerciseRepository.createExercise(exercise) }
                .onSuccess { updateState { copy(isSaving = false, isCreated = true) } }
                .onFailure { error ->
                    updateState {
                        copy(
                            isSaving = false,
                            errorMessage = error.message
                        ).withSubmitEnabled()
                    }
                }
        }
    }

    /** Безымянное упражнение создать нельзя; во время записи повторный сабмит запрещён. */
    private fun ExerciseCreatorUiState.withSubmitEnabled(): ExerciseCreatorUiState =
        copy(isSubmitEnabled = exercise.name.isNotBlank() && !isSaving)

    private fun WorkoutExerciseEntity.normalized(): WorkoutExerciseEntity =
        copy(name = name.trim(), description = description?.trim()?.ifBlank { null })
}
