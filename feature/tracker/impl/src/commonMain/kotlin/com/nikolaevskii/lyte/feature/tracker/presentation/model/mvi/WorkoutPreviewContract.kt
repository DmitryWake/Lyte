package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutPreviewUiModel

/**
 * Превью программы перед стартом (спека 4.2). Экран read-only: держит уже готовую к отрисовке модель
 * [program] (домен смаппен во ViewModel), из которой рендерится состав и план подходов.
 *
 * Стартуем сразу в [isLoading] = true: экран всегда открывается с загрузки по id, поэтому пустого кадра
 * «нет данных» до первого запроса быть не должно. [program] `null`, пока идёт загрузка или произошла
 * ошибка. [isStarting] — идёт создание сессии: guard от дабл-тапа по «Начать тренировку».
 */
data class WorkoutPreviewUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val program: WorkoutPreviewUiModel? = null,
    val isStarting: Boolean = false,
) : UiState

sealed interface WorkoutPreviewIntent : UiIntent {

    /** Старт сессии по программе: снапшот в БД и переход на экран активной сессии (спека 4.3). */
    data object OnStartClicked : WorkoutPreviewIntent

    data object OnBack : WorkoutPreviewIntent
}
