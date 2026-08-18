package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.session.LyteTrackSetState

private const val REPS_STEP = 1
private const val WEIGHT_STEP = 2.5

/**
 * Подходы упражнения в готовом для списка виде: текущий — фокус-карточка с драфтами степперов,
 * остальные — спокойные строки со своим исходом. Живёт рядом с `toActiveSessionUiModel`, а не в
 * экране: экран рисует то, что ему дали, и не считает состояние компонентов сам.
 *
 * Драфты приходят параметрами, потому что их владелец — состояние экрана, а не сессия: они меняются
 * на каждый тап по степперу и в доменную модель не попадают до нажатия «Готово».
 *
 * Ориентир «В прошлый раз» ([LyteTrackSetState.Current.last]) не заполняется: фактов предыдущей
 * сессии домен не хранит (см. KDoc `toActiveSessionUiModel`).
 */
internal fun ActiveSessionCurrentUiModel.toTrackSetStates(
    draftReps: Int,
    draftWeight: Double,
): List<LyteTrackSetState> = sets.map { set ->
    if (set.status == ActiveSessionSetStatus.Current) {
        LyteTrackSetState.Current(
            total = setCount,
            reps = draftReps,
            // Вес передаётся и у цели «свой вес»: ноль — это «пока без веса», и именно так к
            // подтягиваниям добавляют пояс.
            weight = draftWeight,
            target = target,
            repsStep = REPS_STEP,
            weightStep = WEIGHT_STEP,
        )
    } else {
        LyteTrackSetState.Resting(
            tone = set.status.toTone(),
            value = set.value,
            note = set.note.takeIf { note -> note.isNotEmpty() },
        )
    }
}

/**
 * Подпись хвоста списка: есть, только когда текущий подход последний в упражнении. Что дальше —
 * следующее упражнение или конец тренировки — решает позиция упражнения в сессии.
 */
internal fun ActiveSessionCurrentUiModel.lastSetLabel(): ActiveSessionLastSetLabel? = when {
    currentSetIndex != sets.lastIndex -> null
    exerciseIndex == exerciseCount -> ActiveSessionLastSetLabel.LastInSession
    else -> ActiveSessionLastSetLabel.LastInExercise
}

/** Общий словарь исходов: тот же тон, что в треке сводки и в деталях завершённой сессии. */
private fun ActiveSessionSetStatus.toTone(): LyteProgressTone = when (this) {
    ActiveSessionSetStatus.Hit -> LyteProgressTone.Met
    ActiveSessionSetStatus.Exceeded -> LyteProgressTone.Positive
    ActiveSessionSetStatus.Missed -> LyteProgressTone.Negative
    ActiveSessionSetStatus.Skipped -> LyteProgressTone.Skipped
    // Текущий подход рисуется фокус-карточкой и до тона не доходит; Todo — будущий подход.
    ActiveSessionSetStatus.Todo, ActiveSessionSetStatus.Current -> LyteProgressTone.Todo
}
