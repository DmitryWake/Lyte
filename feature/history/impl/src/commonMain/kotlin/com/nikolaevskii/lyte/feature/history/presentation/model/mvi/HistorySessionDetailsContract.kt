package com.nikolaevskii.lyte.feature.history.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.history.presentation.model.HistorySessionDetailsUiModel

/**
 * Состояние экрана деталей сессии (5.2) как набор взаимоисключающих состояний. Сессия завершена и
 * неизменна — грузится один раз, без reload; отсутствие сессии ([Content] недостижим) — это [Error].
 */
sealed interface HistorySessionDetailsUiState : UiState {

    data object Loading : HistorySessionDetailsUiState

    /** Загрузка не удалась или сессия не найдена; [message] — сырой текст ошибки (может быть null). */
    data class Error(val message: String?) : HistorySessionDetailsUiState

    data class Content(val details: HistorySessionDetailsUiModel) : HistorySessionDetailsUiState
}

/** События экрана деталей сессии; решение принимает `HistorySessionDetailsViewModel`. */
sealed interface HistorySessionDetailsIntent : UiIntent {

    /** Пользователь нажал «назад». */
    data object OnBackClicked : HistorySessionDetailsIntent
}
