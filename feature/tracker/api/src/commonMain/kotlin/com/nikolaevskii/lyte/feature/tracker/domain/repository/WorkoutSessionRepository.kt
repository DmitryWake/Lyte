package com.nikolaevskii.lyte.feature.tracker.domain.repository

import com.nikolaevskii.lyte.feature.tracker.domain.model.LastSessionDateEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity

/**
 * Владелец данных сессий тренировки (SSOT — локальная БД). Активная сессия переживает смерть процесса.
 * Единственная точка записи прогресса трекинга и чтения завершённых сессий историей.
 */
interface WorkoutSessionRepository {

    /** Активная (незавершённая) сессия или `null`, если её нет. */
    suspend fun getActiveSession(): WorkoutSessionEntity?

    suspend fun getSession(id: String): WorkoutSessionEntity?

    /** Завершённые сессии для списка истории, свежие первыми. */
    suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity>

    /** Дата последней завершённой сессии по каждой программе — для карточек списка программ. */
    suspend fun getLastSessionDates(): List<LastSessionDateEntity>

    /**
     * Создаёт сессию как снапшот программы (имена и цели копируются, факты пустые).
     *
     * @return id созданной сессии.
     * @throws IllegalStateException если активная сессия уже существует.
     */
    suspend fun startSession(workout: WorkoutEntity): String

    suspend fun completeSet(setId: String, count: Int, weight: Double?)

    suspend fun skipSet(setId: String)

    suspend fun saveSetNote(setId: String, note: String)

    suspend fun setCurrentExercise(sessionId: String, sessionExerciseId: String)

    /**
     * Завершает сессию: все ещё не выполненные подходы помечаются пропущенными, ставится время
     * завершения. Покрывает и обычное завершение (незакрытых подходов нет), и досрочное.
     */
    suspend fun finishSession(id: String)
}
