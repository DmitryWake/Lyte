package com.nikolaevskii.lyte.core.session.data.repository

import com.nikolaevskii.lyte.core.db.session.SessionSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDao
import com.nikolaevskii.lyte.core.session.data.mapper.toDomainEntity
import com.nikolaevskii.lyte.core.session.data.mapper.toItemEntity
import com.nikolaevskii.lyte.core.session.data.mapper.toOutcomesBySession
import com.nikolaevskii.lyte.core.session.data.mapper.toPreviousSetResults
import com.nikolaevskii.lyte.core.session.data.mapper.toSessionRows
import com.nikolaevskii.lyte.core.session.domain.applyProgressionTo
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.core.session.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class WorkoutSessionRepositoryImpl(
    private val workoutSessionDao: WorkoutSessionDao,
    private val workoutRepository: WorkoutRepository,
    private val clock: Clock,
) : WorkoutSessionRepository {

    override suspend fun getActiveSession(): WorkoutSessionEntity? =
        workoutSessionDao.getActiveSession()?.toDomainEntity()

    override suspend fun getSession(id: String): WorkoutSessionEntity? =
        workoutSessionDao.getSession(id)?.toDomainEntity()

    /**
     * Ориентиры на всю сессию — **один** плоский запрос, а не запрос на подход: подходы завершённых
     * сессий этой программы приезжают свежими вперёд, сопоставляет их [toPreviousSetResults].
     *
     * Сопоставление в Kotlin, а не оконной функцией в SQL: правило «какой подход считается тем же»
     * — доменное, и в `commonTest` оно проверяется на обеих платформах, тогда как тесты
     * `:core:core-db` живут только в `androidHostTest` (Robolectric).
     */
    override suspend fun getPreviousSetResults(session: WorkoutSessionEntity): Map<String, SessionSetValueEntity> {
        // Контракт «только для активной сессии» держим проверкой, а не прозой в KDoc: для завершённой
        // сессии выборка вернула бы её собственные факты (она свежайшая по finished_at), и ответ
        // выглядел бы правдоподобным. Правдоподобный неверный ответ хуже отказа.
        require(session.finishedAt == null) {
            "getPreviousSetResults рассчитан на активную сессию, а ${session.id} уже завершена"
        }
        return workoutSessionDao.getProgramSetHistory(session.program.id).toPreviousSetResults(session)
    }

    /**
     * Список истории — **два** запроса, а не запрос на карточку: строки сессий и подходы всех
     * завершённых сессий разом. Исход подхода считается в общей доменной точке правды
     * ([com.nikolaevskii.lyte.core.session.domain.util.outcome]), а не в SQL.
     */
    override suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity> {
        val outcomes = workoutSessionDao.getFinishedSessionSets().toOutcomesBySession()
        return workoutSessionDao.getFinishedSessions().mapNotNull { session ->
            session.toItemEntity(setOutcomes = outcomes[session.id].orEmpty())
        }
    }

    override fun observeFinishedSessions(): Flow<List<WorkoutSessionItemEntity>> =
        combine(
            workoutSessionDao.observeFinishedSessions(),
            workoutSessionDao.observeFinishedSessionSets(),
        ) { sessions, setRows ->
            val outcomes = setRows.toOutcomesBySession()
            sessions.mapNotNull { session ->
                session.toItemEntity(setOutcomes = outcomes[session.id].orEmpty())
            }
        }

    override suspend fun deleteSession(id: String) {
        workoutSessionDao.deleteSession(id)
    }

    override suspend fun startSession(workout: WorkoutEntity): String {
        val rows = workout.toSessionRows(sessionId = Uuid.random().toString(), startedAt = clock.now())
        workoutSessionDao.insertSessionGraph(
            session = rows.session,
            exercises = rows.exercises,
            sets = rows.sets,
        )
        return rows.session.id
    }

    override suspend fun completeSet(setId: String, count: Int, weight: Double?) {
        workoutSessionDao.updateSetResult(
            id = setId,
            status = SessionSetDatabaseEntity.RESULT_STATUS_COMPLETED,
            count = count,
            weight = weight,
        )
    }

    override suspend fun skipSet(setId: String) {
        workoutSessionDao.updateSetResult(
            id = setId,
            status = SessionSetDatabaseEntity.RESULT_STATUS_SKIPPED,
            count = null,
            weight = null,
        )
    }

    override suspend fun saveSetNote(setId: String, note: String) {
        workoutSessionDao.updateSetNote(id = setId, note = note)
    }

    override suspend fun setCurrentExercise(sessionId: String, sessionExerciseId: String) {
        workoutSessionDao.updateCurrentExercise(id = sessionId, exerciseId = sessionExerciseId)
    }

    override suspend fun finishSession(id: String) {
        workoutSessionDao.finishSession(id = id, finishedAt = clock.now().toEpochMilliseconds())
        applyProgression(sessionId = id)
    }

    /**
     * Прогрессия плана — часть завершения сессии: цели программы подтягиваются под факты
     * ([applyProgressionTo]). Сессия перечитывается уже завершённой, поэтому в план не попадут
     * подходы, которые завершение только что пометило пропущенными.
     *
     * Пишем узким [WorkoutRepository.updateWorkoutTargets], а не `editWorkout`: тот пересобрал бы граф
     * программы целиком ради правки одних лишь целей. Пропавшая сессия или программа — не ошибка:
     * обновлять нечего. Программу, удалённую во время сессии, [WorkoutRepository.getWorkout] уже не
     * отдаёт, поэтому прогрессия молча пропускается — цели у удалённой программы никто не увидит.
     */
    private suspend fun applyProgression(sessionId: String) {
        val session = workoutSessionDao.getSession(sessionId)?.toDomainEntity() ?: return
        val workout = workoutRepository.getWorkout(session.program.id) ?: return
        workoutRepository.updateWorkoutTargets(session.applyProgressionTo(workout))
    }
}
