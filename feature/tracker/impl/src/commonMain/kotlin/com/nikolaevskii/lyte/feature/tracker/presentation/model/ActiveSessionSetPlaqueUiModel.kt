package com.nikolaevskii.lyte.feature.tracker.presentation.model

/**
 * Статус плашки подхода в карусели обзора. Нейтрален к дизайн-системе: в `LyteSetOverviewTone`
 * его переводит Compose-слой. Разрешённые подходы ([Hit]/[Exceeded]/[Missed]) показывают факт,
 * [Current]/[Todo] — цель, [Skipped] — прочерк.
 */
enum class ActiveSessionSetStatus { Current, Hit, Exceeded, Missed, Skipped, Todo }

/**
 * Плашка одного подхода в карусели обзора. [index] 1-based — для подписи «№n»; [value] `null` только
 * у пропущенного подхода (рендерится прочерком).
 */
data class ActiveSessionSetPlaqueUiModel(
    val index: Int,
    val status: ActiveSessionSetStatus,
    val value: ActiveSessionSetValueUiModel?,
)
