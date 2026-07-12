package com.nikolaevskii.lyte.feature.tracker.presentation.model

/**
 * Фокус трекинга: эффективное текущее упражнение и его текущий подход. Индексы 1-based — готовы для
 * подписей «Упражнение i из n» / «Подход j из k» без арифметики в Compose.
 *
 * [currentSetId] — цель мутаций (готово/пропустить/заметка) и ключ перезаполнения драфтов степперов:
 * драфты сбрасываются на цель подхода только при его смене. [targetWeight] `null` — подход со своим
 * весом (bodyweight): секция веса не показывается, в факт уходит вес `null`.
 */
data class ActiveSessionCurrentUiModel(
    val exerciseId: String,
    val exerciseIndex: Int,
    val exerciseCount: Int,
    val exerciseName: String,
    val plaques: List<ActiveSessionSetPlaqueUiModel>,
    val currentPlaqueIndex: Int,
    val setIndex: Int,
    val setCount: Int,
    val currentSetId: String,
    val targetReps: Int,
    val targetWeight: Double?,
    val target: ActiveSessionSetValueUiModel,
    val note: String,
)
