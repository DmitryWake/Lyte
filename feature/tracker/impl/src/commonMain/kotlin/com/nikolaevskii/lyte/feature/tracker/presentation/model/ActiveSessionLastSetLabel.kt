package com.nikolaevskii.lyte.feature.tracker.presentation.model

/**
 * Подпись хвоста списка подходов. Не строка, а выбор: строковый ресурс подставляет экран, а решает,
 * какую подпись показать (и показывать ли вообще — `null`), маппер.
 *
 * [LastInExercise] — впереди ещё есть упражнения, [LastInSession] — это последний подход тренировки.
 */
enum class ActiveSessionLastSetLabel { LastInExercise, LastInSession }
