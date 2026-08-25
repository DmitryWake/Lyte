package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ProgramPickerUiModel

/**
 * Лендинг — гейт активной сессии (спека 4.3): на входе проверяем БД и, если сессия есть, уводим на её
 * маршрут вместо отрисовки лендинга.
 */
sealed interface TrackerLandingUiState : UiState {

    /** Идёт проверка БД: экран пуст — найдётся сессия, уйдём на её маршрут без вспышки «Нет сессии». */
    data object CheckingSession : TrackerLandingUiState

    /**
     * Активной сессии нет (в т.ч. проверка упала — вкладку не блокируем, показываем лендинг).
     * [picker] — шторка выбора программы: `null`, пока она закрыта.
     */
    data class NoActiveSession(val picker: ProgramPickerUiModel? = null) : TrackerLandingUiState
}

sealed interface TrackerLandingIntent : UiIntent {

    /** «Начать» — открыть шторку выбора программы и загрузить программы. */
    data object OnStartClicked : TrackerLandingIntent

    /** Шторку закрыли свайпом, тапом по скриму или системной «назад». */
    data object OnPickerDismissed : TrackerLandingIntent

    /** Выбрана программа — уходим на её превью. */
    data class OnProgramClicked(val id: String) : TrackerLandingIntent

    /** Программ нет — уходим на вкладку «Программы» в редактор новой. */
    data object OnCreateProgramClicked : TrackerLandingIntent
}
