package com.nikolaevskii.lyte.core.session.domain.model

import kotlin.time.Instant

/**
 * Лёгкая проекция завершённой сессии для списка истории — без дерева упражнений, только агрегаты:
 * [completedSetCount] из [totalSetCount] подходов выполнено. Длительность потребитель считает как
 * [finishedAt] − [startedAt].
 */
data class WorkoutSessionItemEntity(
    val id: String,
    val program: SessionProgramEntity,
    val startedAt: Instant,
    val finishedAt: Instant,
    val completedSetCount: Int,
    val totalSetCount: Int,
)
