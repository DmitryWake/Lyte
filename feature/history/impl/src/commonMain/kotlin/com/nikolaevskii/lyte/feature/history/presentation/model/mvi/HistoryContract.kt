package com.nikolaevskii.lyte.feature.history.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.history.presentation.model.HistoryMonthGroupUiModel

/**
 * Состояние экрана Истории (5.1) как набор взаимоисключающих состояний — так рендер экрана становится
 * исчерпывающим `when` без комбинаций флагов и невозможных сочетаний. Обновление, у которого уже есть
 * [Content], не сбрасывает список в [Loading]/[Error] (см. `HistoryViewModel.loadSessions`).
 */
sealed interface HistoryUiState : UiState {

    /** Первичная загрузка — показать индикатор (данных ещё нет). */
    data object Loading : HistoryUiState

    /** Загрузка не удалась и показывать нечего; [message] — сырой текст ошибки (может быть null). */
    data class Error(val message: String?) : HistoryUiState

    /** Завершённых сессий нет — пустое состояние. */
    data object Empty : HistoryUiState

    /** Есть сессии; [groups] непустой и сгруппирован по месяцам (убыв.). */
    data class Content(val groups: List<HistoryMonthGroupUiModel>) : HistoryUiState
}

/** События экрана списка сессий; решение принимает `HistoryViewModel`. */
sealed interface HistoryIntent : UiIntent {

    /** Экран показан — перечитываем список, чтобы подхватить сессии, завершённые в трекере. */
    data object OnScreenShown : HistoryIntent

    /** Тап по завершённой сессии [id] — открыть её детали (5.2). */
    data class OnSessionClicked(val id: String) : HistoryIntent
}
