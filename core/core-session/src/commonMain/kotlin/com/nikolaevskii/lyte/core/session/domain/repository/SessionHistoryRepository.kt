package com.nikolaevskii.lyte.core.session.domain.repository

import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Чтение завершённых сессий — контракт для истории. Узкий read-интерфейс (ISP): история не видит
 * write-поверхность трекинга (её несёт [WorkoutSessionRepository]).
 */
interface SessionHistoryRepository {

    /** Завершённые сессии для списка истории, свежие первыми. */
    suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity>

    /** Реактивный список завершённых сессий (SSOT — БД): эмитит при завершении новой сессии. */
    fun observeFinishedSessions(): Flow<List<WorkoutSessionItemEntity>>

    suspend fun getSession(id: String): WorkoutSessionEntity?
}
