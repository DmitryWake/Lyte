package com.nikolaevskii.lyte.feature.tracker.presentation.model

/**
 * Статус подхода в списке экрана тренировки. Нейтрален к дизайн-системе: в `LyteProgressTone`
 * его переводит `toTrackSetStates`. Разрешённые подходы ([Hit]/[Exceeded]/[Missed]) показывают факт,
 * [Current]/[Todo] — цель, [Skipped] — «пропущен».
 */
enum class ActiveSessionSetStatus { Current, Hit, Exceeded, Missed, Skipped, Todo }
