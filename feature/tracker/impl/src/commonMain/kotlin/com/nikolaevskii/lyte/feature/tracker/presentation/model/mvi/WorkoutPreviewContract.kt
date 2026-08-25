package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewExerciseUiModel
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
        /**
         * Шторка с описанием упражнения (кадр `preview-exercise`): `null`, пока она закрыта.
         *
         * Держит саму модель, а не номер: состав уже загружен и больше не меняется, поэтому искать
         * упражнение в списке пришлось бы композаблу — а экран только рендерит. Своей sealed-модели
         * содержимого, в отличие от шторки выбора программы на лендинге, здесь не нужно: грузить
         * нечего, значит нет ни арма загрузки, ни арма ошибки.
         */
        val exerciseInfo: WorkoutPreviewExerciseUiModel? = null,
    ) : WorkoutPreviewUiState
}

sealed interface WorkoutPreviewIntent : UiIntent {

    /** Старт сессии по программе: снапшот в БД и переход на экран активной сессии (спека 4.3). */
    data object OnStartClicked : WorkoutPreviewIntent

    /** Тап по карточке — открыть шторку с описанием упражнения и его плановыми подходами. */
    data class OnExerciseClicked(val number: Int) : WorkoutPreviewIntent

    /** Шторку закрыли свайпом, тапом по скриму или системной «назад». */
    data object OnExerciseInfoDismissed : WorkoutPreviewIntent

    data object OnBack : WorkoutPreviewIntent
}
