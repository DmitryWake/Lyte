package com.nikolaevskii.lyte.feature.history.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.history.presentation.model.HistorySessionDetailsUiModel

/**
 * Состояние экрана деталей сессии (5.2) как набор взаимоисключающих состояний. Сессия завершена и
 * неизменна — грузится один раз, без reload; отсутствие сессии ([Content] недостижим) — это [Error].
 */
sealed interface HistorySessionDetailsUiState : UiState {

    data object Loading : HistorySessionDetailsUiState

    /** Загрузка не удалась или сессия не найдена. */
    data class Error(val error: LyteError) : HistorySessionDetailsUiState

    data class Content(
        val details: HistorySessionDetailsUiModel,
        /** Диалог подтверждения удаления существует только над контентом; имя программы берётся из [details]. */
        val isDeleteDialogVisible: Boolean = false,
        /**
         * Удаление уже запущено: guard от дабл-тапа по «Удалить» (второй `DELETE` идемпотентен и тоже
         * успешен, поэтому без guard'а в навигацию ушёл бы второй `back` — он снял бы стартовый
         * destination вкладки Истории). Пока флаг поднят, действие удаления в шапке заменено индикатором.
         */
        val isDeleting: Boolean = false,
        /** Неудачное удаление — баннер над деталями (детали остаются на экране). */
        val actionError: LyteError? = null,
    ) : HistorySessionDetailsUiState
}

/** События экрана деталей сессии; решение принимает `HistorySessionDetailsViewModel`. */
sealed interface HistorySessionDetailsIntent : UiIntent {

    /** Пользователь нажал «назад». */
    data object OnBackClicked : HistorySessionDetailsIntent

    /** Пользователь нажал «удалить» в шапке. */
    data object OnDeleteClicked : HistorySessionDetailsIntent

    /** Пользователь подтвердил удаление сессии. */
    data object OnDeleteConfirmed : HistorySessionDetailsIntent

    /** Пользователь закрыл диалог подтверждения удаления. */
    data object OnDeleteDismissed : HistorySessionDetailsIntent
}
