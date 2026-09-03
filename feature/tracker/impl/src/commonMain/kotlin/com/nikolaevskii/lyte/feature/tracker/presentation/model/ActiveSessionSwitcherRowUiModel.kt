package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.model.LyteSetValue

/** Статус строки в шторке «Упражнения сессии». */
enum class ActiveSessionSwitcherStatus { Current, Done, Pending }

/**
 * Строка упражнения в шторке переключения. [doneCount] — разрешённые подходы (выполненные и
 * пропущенные). [currentSetIndex] 1-based, только у текущего упражнения — для подписи
 * «подход j из k». [targetPills] — цели всех подходов, только у ещё не начатых упражнений
 * (`Pending` и [doneCount] = 0); у начатого подпись показывает счёт «[doneCount] из [setCount]».
 * [isSelectable] `false` у полностью закрытых: выбирать их текущими нельзя (нечего трекать).
 */
data class ActiveSessionSwitcherRowUiModel(
    val exerciseId: String,
    val name: String,
    val status: ActiveSessionSwitcherStatus,
    val doneCount: Int,
    val setCount: Int,
    val currentSetIndex: Int?,
    val targetPills: List<LyteSetValue>,
    val isSelectable: Boolean,
)
