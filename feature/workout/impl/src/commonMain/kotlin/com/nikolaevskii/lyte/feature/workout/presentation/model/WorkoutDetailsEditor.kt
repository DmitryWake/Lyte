package com.nikolaevskii.lyte.feature.workout.presentation.model

/**
 * Что открыто над формой редактора программы. Взаимоисключающие состояния (`null` — ничего): редактор
 * подходов, шторка выбора упражнения из библиотеки, шторка создания нового или шторка маркера
 * программы. Sealed-модель заменяет пару независимых nullable-полей (`editingExerciseIndex` +
 * `exerciseSheet`) — два оверлея одновременно теперь невыразимы по построению.
 */
sealed interface WorkoutDetailsEditor {

    /** Редактор подходов упражнения [exerciseIndex]. */
    data class SetsEditor(val exerciseIndex: Int) : WorkoutDetailsEditor

    /**
     * Выбор упражнения из библиотеки. [query] — предзаполненный поиск: возвращается из [ExerciseCreator]
     * при отмене создания, поэтому передаётся явно.
     */
    data class ExercisePicker(val query: String = "") : WorkoutDetailsEditor

    /** Создание нового упражнения; [initialName] — поиск, с которым ушли из [ExercisePicker]. */
    data class ExerciseCreator(val initialName: String) : WorkoutDetailsEditor

    /** Шторка «Цвет и знак»: маркер программы. Своих полей нет — цвет и знак живут в черновике формы. */
    data object Mark : WorkoutDetailsEditor
}
