package com.nikolaevskii.lyte.feature.tracker.presentation.model

/**
 * Активный оверлей экрана активной сессии. Состояния взаимоисключающие — «двух шторок разом» не бывает,
 * поэтому это единый sealed-тип, а не набор булевых флагов. Хранится в поле `overlay` UiState экрана.
 */
sealed interface ActiveSessionOverlayUiModel {

    /** Оверлея нет. */
    data object None : ActiveSessionOverlayUiModel

    /** Шторка «Упражнения сессии» — переключение текущего упражнения. */
    data object ExerciseSheet : ActiveSessionOverlayUiModel

    /** Шторка заметки к текущему подходу. [draft] — редактируемый буфер, в БД уходит по «Готово». */
    data class NoteSheet(val draft: String) : ActiveSessionOverlayUiModel

    /** Диалог подтверждения досрочного завершения. */
    data object EndEarlyDialog : ActiveSessionOverlayUiModel
}
