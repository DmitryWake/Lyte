package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.mvi.LyteError

/**
 * Содержимое шторки выбора программы на лендинге. Сам факт «шторка открыта» несёт `null`-ность поля
 * `picker` в состоянии экрана: армы здесь отвечают только на вопрос «что показать внутри», и
 * отдельный арм «закрыта» смешал бы два разных вопроса в одном типе.
 */
sealed interface ProgramPickerUiModel {

    /** Программы читаются из БД. */
    data object Loading : ProgramPickerUiModel

    /** Список программ пользователя. */
    data class Programs(val programs: List<WorkoutProgramUiModel>) : ProgramPickerUiModel

    /** Программ нет ни одной — предлагаем создать первую. */
    data object Empty : ProgramPickerUiModel

    /** Прочитать программы не удалось. */
    data class Error(val error: LyteError) : ProgramPickerUiModel
}
