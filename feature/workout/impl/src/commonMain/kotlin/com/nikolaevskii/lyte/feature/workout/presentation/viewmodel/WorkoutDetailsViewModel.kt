@file:OptIn(ExperimentalUuidApi::class)

package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.mvi.LyteNotFoundException
import com.nikolaevskii.lyte.core.mvi.toLyteError
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutDetailsEditor
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseUiModel
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState.WorkoutDetailsContent
import com.nikolaevskii.lyte.feature.workout.presentation.model.toUiModel
import kotlinx.coroutines.CancellationException
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
            is WorkoutDetailsIntent.OnNameChanged -> updateEditing { copy(name = intent.name) }
            WorkoutDetailsIntent.OnMarkClicked -> updateEditing { copy(editor = WorkoutDetailsEditor.Mark) }
            // Шторка не закрывается на выбор: в макете цвет и знак подбирают вместе, и пикер знаков
            // перекрашивается вслед за цветом — закрытие после первого тапа сломало бы этот подбор.
            is WorkoutDetailsIntent.OnAccentChanged -> updateEditing { copy(accent = intent.accent) }
            is WorkoutDetailsIntent.OnGlyphChanged -> updateEditing { copy(glyph = intent.glyph) }
            WorkoutDetailsIntent.OnMarkSheetDismissed -> updateEditing { copy(editor = null) }
            is WorkoutDetailsIntent.OnExerciseMoved -> updateEditing { copy(exercises = exercises.moved(intent.fromIndex, intent.toIndex)) }
            is WorkoutDetailsIntent.OnRemoveExerciseClicked -> updateEditing { copy(exercises = exercises.filterIndexed { i, _ -> i != intent.index }) }
            WorkoutDetailsIntent.OnAddExerciseClicked -> updateEditing { copy(editor = WorkoutDetailsEditor.ExercisePicker()) }
            WorkoutDetailsIntent.OnExerciseSheetDismissed -> dismissExerciseSheet()
            is WorkoutDetailsIntent.OnCreateExerciseClicked ->
                updateEditing { copy(editor = WorkoutDetailsEditor.ExerciseCreator(initialName = intent.query)) }
            is WorkoutDetailsIntent.OnExerciseSelected -> addExercise(intent.exercise)
            is WorkoutDetailsIntent.OnEditSetsClicked -> updateEditing { copy(editor = WorkoutDetailsEditor.SetsEditor(exerciseIndex = intent.index)) }
            WorkoutDetailsIntent.OnSetsEditorDismissed -> updateEditing { copy(editor = null) }
            is WorkoutDetailsIntent.OnSetRepsChanged ->
                updateEditedSets { sets -> sets.mapIndexed { i, set -> if (i == intent.setIndex) set.copy(count = intent.reps) else set } }
            is WorkoutDetailsIntent.OnSetWeightChanged ->
                updateEditedSets { sets -> sets.mapIndexed { i, set -> if (i == intent.setIndex) set.copy(weight = intent.weight) else set } }
            WorkoutDetailsIntent.OnAddSetClicked -> updateEditedSets { sets -> sets + (sets.lastOrNull() ?: DEFAULT_REP) }
            is WorkoutDetailsIntent.OnRemoveSetClicked -> updateEditedSets { sets -> sets.filterIndexed { i, _ -> i != intent.setIndex } }
            WorkoutDetailsIntent.OnDoneClicked -> save()
            WorkoutDetailsIntent.OnBackClicked -> lyteNavigator.back()
        }
    }

    override fun getInitialState(): WorkoutDetailsUiState = WorkoutDetailsUiState(
        id = initialId ?: Uuid.random().toString(),
        // Новая программа открывается сразу в редакторе (пустая форма), не в Loading — иначе вечный спиннер.
        content = if (initialId == null) {
            WorkoutDetailsContent.Editing(
                name = "",
                description = null,
                accent = ExerciseAccent.Default,
                glyph = ExerciseGlyph.Default,
                exercises = emptyList(),
            )
        } else {
            WorkoutDetailsContent.Loading
        },
    )

    // Провал загрузки уходит в воронку handleError → Error: формы и кнопки «Готово» нет, поэтому
    // перезаписать реальную программу пустой (баг потери данных) невозможно по построению.
    override fun handleError(error: Throwable) {
        updateState { copy(content = WorkoutDetailsContent.Error(error.toLyteError())) }
    }

    private suspend fun loadWorkout(id: String) {
        updateState { copy(content = WorkoutDetailsContent.Loading) }
        val workout = workoutRepository.getWorkout(id) ?: throw LyteNotFoundException("Workout $id not found")
        updateState {
            copy(
                content = WorkoutDetailsContent.Editing(
                    name = workout.name,
                    description = workout.description,
                    accent = workout.accent,
                    glyph = workout.glyph,
                    exercises = workout.exercises.map { it.toUiModel() },
                ),
            )
        }
    }

    private fun save() {
        val editing = uiStateValue.content as? WorkoutDetailsContent.Editing ?: return
        val id = uiStateValue.id
        updateEditing { copy(isSaving = true, saveError = null) }
        launch {
            // Маркер обязателен в сборке сущности: у accent/glyph в WorkoutEntity есть значения по
            // умолчанию, и пропуск полей молча перекрасил бы программу в slate/squat при каждом
            // сохранении.
            val workoutEntity = WorkoutEntity(
                id = id,
                name = editing.name,
                description = editing.description,
                accent = editing.accent,
                glyph = editing.glyph,
                exercises = editing.exercises.map { it.exercise },
            )
            runCatching {
                if (initialId == null) {
                    workoutRepository.createWorkout(workoutEntity)
                } else {
                    workoutRepository.editWorkout(workoutEntity)
                }
            }
                .onSuccess {
                    updateEditing { copy(isSaving = false) }
                    lyteNavigator.back()
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    updateEditing { copy(isSaving = false, saveError = error.toLyteError()) }
                }
        }
    }

    /**
     * Закрытие формы создания — это возврат к шторке выбора, а не выход: пользователь передумал
     * создавать упражнение, но не искать его. Поисковый запрос возвращаем тот же, с которым ушли.
     */
    private fun dismissExerciseSheet() = updateEditing {
        copy(
            editor = when (val editor = editor) {
                is WorkoutDetailsEditor.ExerciseCreator -> WorkoutDetailsEditor.ExercisePicker(query = editor.initialName)
                is WorkoutDetailsEditor.ExercisePicker -> null
                is WorkoutDetailsEditor.SetsEditor, WorkoutDetailsEditor.Mark, null -> editor
            },
        )
    }

    /**
     * Упражнение добавляется без подходов, поэтому шторка закрывается сразу в редактор подходов —
     * иначе упражнение осталось бы в программе с пустым планом.
     */
    private fun addExercise(exercise: WorkoutExerciseEntity) = updateEditing {
        val updated = exercises + WorkoutExerciseWithRepsEntity(exercise = exercise, reps = emptyList()).toUiModel()
        copy(exercises = updated, editor = WorkoutDetailsEditor.SetsEditor(exerciseIndex = updated.lastIndex))
    }

    /** Правит форму только когда экран в редактировании; в Loading/Error — no-op. */
    private fun updateEditing(block: WorkoutDetailsContent.Editing.() -> WorkoutDetailsContent.Editing) {
        updateState {
            val editing = content as? WorkoutDetailsContent.Editing ?: return@updateState this
            copy(content = editing.block())
        }
    }

    /** Правит подходы упражнения, открытого в редакторе подходов; иначе no-op. */
    private fun updateEditedSets(transform: (List<WorkoutRepEntity>) -> List<WorkoutRepEntity>) = updateEditing {
        val index = (editor as? WorkoutDetailsEditor.SetsEditor)?.exerciseIndex ?: return@updateEditing this
        copy(
            exercises = exercises.mapIndexed { i, model ->
                if (i == index) model.copy(exercise = model.exercise.copy(reps = transform(model.exercise.reps))) else model
            },
        )
    }

    private fun List<WorkoutExerciseUiModel>.moved(fromIndex: Int, toIndex: Int): List<WorkoutExerciseUiModel> {
        if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) return this
        return toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    private companion object {
        /** Подход по умолчанию: 8 повторений со своим весом — вес пользователь проставит сам. */
        val DEFAULT_REP = WorkoutRepEntity(count = 8, weight = null)
    }
}
