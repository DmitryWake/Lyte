package com.nikolaevskii.lyte.core.db.workout

/**
 * Точечное обновление цели одного подхода программы. Подход адресуется позициями
 * ([exercisePosition] внутри программы, [setPosition] внутри упражнения), а не id строки:
 * доменные модели `:core:core-workout` синтетических id подходов не несут.
 *
 * Нужно, чтобы прогрессия по итогам сессии меняла только цели и не переписывала граф программы
 * целиком через `WorkoutDao.saveWorkoutGraph` (тот пересоздаёт связки и апсертит `workout`/`exercise`,
 * сбрасывая им `is_archived`).
 */
data class WorkoutSetTargetUpdate(
    val exercisePosition: Int,
    val setPosition: Int,
    val count: Int,
    val weight: Double?,
)
