package com.nikolaevskii.lyte.core.session.domain.model

import kotlin.time.Instant

/**
 * Лёгкая проекция завершённой сессии для списка истории — без дерева упражнений, только снапшот
 * программы и исходы подходов. Длительность потребитель считает как [finishedAt] − [startedAt].
 *
 * [setOutcomes] — по одному исходу на подход в порядке сессии (упражнение → подход): из него
 * карточка истории рисует трек. `null` в списке — подход без результата; у завершённой сессии
 * таких нет (завершение помечает незакрытые подходы пропущенными), но модель этого не гарантирует.
 */
data class WorkoutSessionItemEntity(
    val id: String,
    val program: SessionProgramEntity,
    val startedAt: Instant,
    val finishedAt: Instant,
    val setOutcomes: List<SessionSetOutcomeEntity?>,
)
