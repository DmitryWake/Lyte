package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.component.feedback.LyteDiffTone

/**
 * Строка подхода в диффе деталей сессии (5.2): план→факт с [tone] (итог против цели). [actual] `null`
 * для пропущенного/невыполненного подхода — [LyteDiffTone.Skipped] всё равно рисует «пропущено».
 * [note] — заметка к подходу или `null`, если пустая.
 */
data class HistoryDiffRowUiModel(
    val id: String,
    val index: Int,
    val tone: LyteDiffTone,
    val target: HistorySetValueUiModel,
    val actual: HistorySetValueUiModel?,
    val note: String?,
)
