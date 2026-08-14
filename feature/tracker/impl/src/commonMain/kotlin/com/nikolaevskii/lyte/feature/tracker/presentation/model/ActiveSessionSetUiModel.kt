package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.model.LyteSetValue

/**
 * Статус подхода в списке экрана тренировки. Нейтрален к дизайн-системе: в `LyteProgressTone`
 * его переводит Compose-слой. Разрешённые подходы ([Hit]/[Exceeded]/[Missed]) показывают факт,
 * [Current]/[Todo] — цель, [Skipped] — «пропущен».
 */
enum class ActiveSessionSetStatus { Current, Hit, Exceeded, Missed, Skipped, Todo }

/**
 * Один подход в списке текущего упражнения. [index] 1-based — для номера строки; [value] `null`
 * только у пропущенного подхода (его строка значения не показывает). Что именно в [value] —
 * решает [status]: у разрешённых это факт, у текущего и будущих — цель.
 *
 * [note] — заметка, написанная к подходу; пустая строка означает «заметки нет».
 */
data class ActiveSessionSetUiModel(
    val index: Int,
    val status: ActiveSessionSetStatus,
    val value: LyteSetValue?,
    val note: String,
)
