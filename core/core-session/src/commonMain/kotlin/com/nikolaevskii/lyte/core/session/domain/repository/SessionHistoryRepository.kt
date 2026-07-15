package com.nikolaevskii.lyte.core.session.domain.repository

import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity

/**
 * Чтение завершённых сессий — контракт для истории. Узкий read-интерфейс (ISP): история не видит
 * write-поверхность трекинга (её несёт [WorkoutSessionRepository]).
 */
interface SessionHistoryRepository {

    /** Завершённые сессии для списка истории, свежие первыми. */
    suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity>

    suspend fun getSession(id: String): WorkoutSessionEntity?
}
