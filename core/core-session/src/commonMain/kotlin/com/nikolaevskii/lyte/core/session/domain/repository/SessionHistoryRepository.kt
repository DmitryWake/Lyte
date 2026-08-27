package com.nikolaevskii.lyte.core.session.domain.repository

import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Контракт истории: чтение завершённых сессий и удаление своей записи. Узкий (ISP) — история не
 * видит write-поверхность трекинга (её несёт [WorkoutSessionRepository]). [deleteSession] живёт
 * здесь, а не там: удалить запись из истории — операция самой истории, и ради неё тянуть в неё
 * старт сессии с записью подходов было бы ровно тем, от чего сплит и защищает.
 */
interface SessionHistoryRepository {

    /** Завершённые сессии для списка истории, свежие первыми. */
    suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity>

    /** Реактивный список завершённых сессий (SSOT — БД): эмитит при завершении новой сессии. */
    fun observeFinishedSessions(): Flow<List<WorkoutSessionItemEntity>>

    suspend fun getSession(id: String): WorkoutSessionEntity?

    /**
     * Удаляет сессию вместе с её упражнениями и подходами. Программу и упражнения библиотеки не
     * трогает — сессия самодостаточна и FK на программу у неё нет.
     */
    suspend fun deleteSession(id: String)
}
