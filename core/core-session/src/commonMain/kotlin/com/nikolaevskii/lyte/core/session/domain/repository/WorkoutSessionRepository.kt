package com.nikolaevskii.lyte.core.session.domain.repository

import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity

/**
 * Владелец данных сессий тренировки (SSOT — локальная БД). Пишет прогресс трекинга и владеет активной
 * сессией (она переживает смерть процесса); чтение завершённых сессий отдаёт через
 * [SessionHistoryRepository]. Единственная реализация регистрируется в Koin под обоими интерфейсами.
 */
interface WorkoutSessionRepository : SessionHistoryRepository {

    /** Активная (незавершённая) сессия или `null`, если её нет. */
    suspend fun getActiveSession(): WorkoutSessionEntity?

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
