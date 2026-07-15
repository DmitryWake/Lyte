package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.core.session.domain.repository.SessionHistoryRepository

/**
 * Дубль read-контракта сессий для тестов Истории: настраиваемый [finishedSessions]/[session] и
 * инъекция ошибок чтения. Благодаря ISP History зависит только от [SessionHistoryRepository], поэтому
 * дублировать write-поверхность трекинга больше не нужно.
 */
internal class FakeSessionHistoryRepository(
    var finishedSessions: List<WorkoutSessionItemEntity> = emptyList(),
    var session: WorkoutSessionEntity? = null,
) : SessionHistoryRepository {

    var getFinishedSessionsError: Throwable? = null
    var getSessionError: Throwable? = null

    override suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity> {
        getFinishedSessionsError?.let { error -> throw error }
        return finishedSessions
    }

    override suspend fun getSession(id: String): WorkoutSessionEntity? {
        getSessionError?.let { error -> throw error }
        return session?.takeIf { candidate -> candidate.id == id }
    }
}
