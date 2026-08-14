package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.model.LyteSetValue

/**
 * Фокус трекинга: эффективное текущее упражнение и его текущий подход. [exerciseIndex] 1-based —
 * готов для подписи «Упражнение i из n» без арифметики в Compose; [currentSetIndex] 0-based —
 * позиция фокус-карточки в [sets], список подходов индексируется от нуля.
 *
 * [currentSetId] — цель мутаций (готово/пропустить/заметка) и ключ перезаполнения драфтов степперов:
 * драфты сбрасываются на цель подхода только при его смене. [targetWeight] `null` — подход со своим
 * весом (bodyweight): степпер веса не показывается, в факт уходит вес `null`.
 */
data class ActiveSessionCurrentUiModel(
    val exerciseId: String,
    val exerciseIndex: Int,
    val exerciseCount: Int,
    val exerciseName: String,
    val sets: List<ActiveSessionSetUiModel>,
    val currentSetIndex: Int,
    val setCount: Int,
    val currentSetId: String,
    val targetReps: Int,
    val targetWeight: Double?,
    val target: LyteSetValue,
    val note: String,
)
