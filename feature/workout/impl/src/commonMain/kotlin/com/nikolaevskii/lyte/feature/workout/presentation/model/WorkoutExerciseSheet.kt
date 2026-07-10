package com.nikolaevskii.lyte.feature.workout.presentation.model

/**
 * Какая шторка добавления упражнения открыта над редактором программы. `null` в
 * `WorkoutDetailsUiState.exerciseSheet` — не открыта ни одна.
 *
 * Именно sealed-модель, а не пара независимых полей: шторки взаимоисключающие (две `ModalBottomSheet`
 * одновременно не показать), и такое состояние невыразимо по построению.
 */
sealed interface WorkoutExerciseSheet {

    /**
     * Выбор упражнения из библиотеки. [query] — предзаполненный поисковый запрос: шторка живёт ровно
     * столько, сколько открыта, поэтому при возврате из [Creator] запрос надо вернуть ей явно.
     */
    data class Picker(val query: String = "") : WorkoutExerciseSheet

    /** Создание нового упражнения; [initialName] — поисковый запрос, с которым ушли из [Picker]. */
    data class Creator(val initialName: String) : WorkoutExerciseSheet
}
