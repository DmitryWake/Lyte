package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.model.LyteSetValue

/**
 * Строка подхода в диффе деталей сессии (5.2): факт против цели с [tone] (итог против цели).
 * [actual] `null` для пропущенного/невыполненного подхода — [LyteProgressTone.Skipped] всё равно
 * рисует «пропущено». [note] — заметка к подходу или `null`, если пустая.
 */
data class HistoryDiffRowUiModel(
    val id: String,
    val index: Int,
    val tone: LyteProgressTone,
    val target: LyteSetValue,
    val actual: LyteSetValue?,
    val note: String?,
)
