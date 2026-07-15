package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewUiModel

/**
 * Превью программы перед стартом (спека 4.2). Экран всегда открывается с загрузки по id.
 */
sealed interface WorkoutPreviewUiState : UiState {

    data object Loading : WorkoutPreviewUiState

    /** Программу не удалось прочитать: ни состава, ни кнопки старта. */
    data class Error(val error: LyteError) : WorkoutPreviewUiState

    data class Content(
        val program: WorkoutPreviewUiModel,
        /** В арме: стартовать можно только загруженную программу (guard от дабл-тапа). */
        val isStarting: Boolean = false,
        /** В арме: неудачный старт — баннер над сохранённым составом, а не подмена экрана. */
        val startError: LyteError? = null,
    ) : WorkoutPreviewUiState
}

sealed interface WorkoutPreviewIntent : UiIntent {

    /** Старт сессии по программе: снапшот в БД и переход на экран активной сессии (спека 4.3). */
    data object OnStartClicked : WorkoutPreviewIntent

    data object OnBack : WorkoutPreviewIntent
}
