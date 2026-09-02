package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.core.session.domain.repository.SessionHistoryRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Дубль контракта истории для её тестов: настраиваемый [finishedSessions]/[session], инъекция ошибок
 * и лог удалений. Благодаря ISP History зависит только от [SessionHistoryRepository], поэтому
 * дублировать write-поверхность трекинга не нужно.
 */
internal class FakeSessionHistoryRepository(
    var finishedSessions: List<WorkoutSessionItemEntity> = emptyList(),
    var session: WorkoutSessionEntity? = null,
) : SessionHistoryRepository {

    var getFinishedSessionsError: Throwable? = null

    /**
     * Шлюз: пока выставлен и не завершён, [getFinishedSessions] висит. Нужен, чтобы тест успел
     * увидеть промежуточный кадр загрузки — без приостановки `StateFlow` схлопнет его с итоговым.
     */
    var getFinishedSessionsGate: CompletableDeferred<Unit>? = null
    var getSessionError: Throwable? = null
    var deleteSessionError: Throwable? = null

    /** Id сессий, для которых вызвали удаление, — в порядке вызова. */
    val deletedSessionIds = mutableListOf<String>()

    override suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity> {
        getFinishedSessionsGate?.await()
        getFinishedSessionsError?.let { error -> throw error }
        return finishedSessions
    }

    override fun observeFinishedSessions(): Flow<List<WorkoutSessionItemEntity>> = flow { emit(getFinishedSessions()) }

    override suspend fun getSession(id: String): WorkoutSessionEntity? {
        getSessionError?.let { error -> throw error }
        return session?.takeIf { candidate -> candidate.id == id }
    }

    override suspend fun deleteSession(id: String) {
        deleteSessionError?.let { error -> throw error }
        deletedSessionIds += id
    }
}
