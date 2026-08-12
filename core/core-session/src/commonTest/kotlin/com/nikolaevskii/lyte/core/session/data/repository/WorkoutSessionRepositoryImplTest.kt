package com.nikolaevskii.lyte.core.session.data.repository

import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class WorkoutSessionRepositoryImplTest {

    @Test
    fun startSessionSnapshotsProgram() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)

        val sessionId = repository.startSession(sampleWorkout())
        val session = repository.getActiveSession()

        assertEquals(sessionId, session?.id)
        assertEquals("prog-1", session?.program?.id)
        assertEquals("Push Day", session?.program?.name)
        // Маркер программы снапшотится в саму сессию — она переживёт перекраску и удаление программы.
        assertEquals(ExerciseAccent.Indigo, session?.program?.accent)
        assertEquals(ExerciseGlyph.BenchPress, session?.program?.glyph)
        assertEquals(Instant.fromEpochMilliseconds(START_MILLIS), session?.startedAt)
        assertNull(session?.finishedAt)
        assertNull(session?.currentExerciseId)

        // Порядок упражнений и подходов восстановлен по position несмотря на перемешивание в фейке.
        assertEquals(listOf("Жим", "Тяга"), session?.exercises?.map { it.exercise.name })
        val firstExercise = session?.exercises?.first()
        assertEquals("Грудь", firstExercise?.exercise?.description)
        // А маркер упражнения — из живой библиотеки, а не из программы: он отличается от её маркера.
        assertEquals(ExerciseAccent.Teal, firstExercise?.exercise?.accent)
        assertEquals(ExerciseGlyph.PullUp, firstExercise?.exercise?.glyph)
        assertEquals(listOf(10, 8), firstExercise?.sets?.map { it.target.count })
        assertEquals(listOf(40.0, 45.0), firstExercise?.sets?.map { it.target.weight })
        // Факты пустые, заметки пустые.
        assertTrue(session?.exercises?.flatMap { it.sets }?.all { it.result == null && it.note == "" } == true)
    }

    @Test
    fun startSessionFailsWhenActiveSessionExists() = runTest {
        val repository = repository()
        repository.startSession(sampleWorkout())

        assertFailsWith<IllegalStateException> {
            repository.startSession(sampleWorkout(id = "prog-2"))
        }
    }

    @Test
    fun completeSetStoresActualValue() = runTest {
        val repository = repository()
        repository.startSession(sampleWorkout())
        val setId = firstSetId(repository)

        repository.completeSet(setId = setId, count = 9, weight = 42.5)

        val result = setById(repository, setId).result
        assertTrue(result is SessionSetResultEntity.Completed)
        assertEquals(9, result.actual.count)
        assertEquals(42.5, result.actual.weight)
    }

    @Test
    fun skipSetStoresSkippedResult() = runTest {
        val repository = repository()
        repository.startSession(sampleWorkout())
        val setId = firstSetId(repository)

        repository.skipSet(setId)

        assertEquals(SessionSetResultEntity.Skipped, setById(repository, setId).result)
    }

    @Test
    fun saveSetNoteStoresNote() = runTest {
        val repository = repository()
        repository.startSession(sampleWorkout())
        val setId = firstSetId(repository)

        repository.saveSetNote(setId = setId, note = "тяжело")

        assertEquals("тяжело", setById(repository, setId).note)
    }

    @Test
    fun setCurrentExerciseIsPersisted() = runTest {
        val repository = repository()
        val sessionId = repository.startSession(sampleWorkout())
        val secondExerciseId = repository.getActiveSession()!!.exercises[1].id

        repository.setCurrentExercise(sessionId = sessionId, sessionExerciseId = secondExerciseId)

        assertEquals(secondExerciseId, repository.getActiveSession()?.currentExerciseId)
    }

    @Test
    fun finishSessionSkipsOnlyPendingAndKeepsResults() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)
        val sessionId = repository.startSession(sampleWorkout())
        val setId = firstSetId(repository)
        repository.completeSet(setId = setId, count = 10, weight = 40.0)

        clock.nowMillis = FINISH_MILLIS
        repository.finishSession(sessionId)

        val session = repository.getSession(sessionId)!!
        assertEquals(Instant.fromEpochMilliseconds(FINISH_MILLIS), session.finishedAt)
        val allSets = session.exercises.flatMap { it.sets }
        // Выполненный подход не тронут, остальные — пропущены; pending не осталось.
        val completed = allSets.single { it.id == setId }.result
        assertTrue(completed is SessionSetResultEntity.Completed)
        assertEquals(10, completed.actual.count)
        assertTrue(allSets.filter { it.id != setId }.all { it.result == SessionSetResultEntity.Skipped })
    }

    @Test
    fun finishSessionWritesActualValuesBackToProgram() = runTest {
        val workoutRepository = FakeWorkoutRepository()
        val workout = sampleWorkout()
        workoutRepository.createWorkout(workout)
        val repository = repository(workoutRepository = workoutRepository)
        val sessionId = repository.startSession(workout)
        val setId = firstSetId(repository)
        repository.completeSet(setId = setId, count = 12, weight = 42.5)

        repository.finishSession(sessionId)

        val progressed = workoutRepository.getWorkout(workout.id)!!
        // Выполненный подход задал новую цель; остальные пропущены завершением — цели прежние.
        assertEquals(
            listOf(WorkoutRepEntity(count = 12, weight = 42.5), WorkoutRepEntity(count = 8, weight = 45.0)),
            progressed.exercises.first().reps,
        )
        assertEquals(listOf(WorkoutRepEntity(count = 12, weight = null)), progressed.exercises[1].reps)
    }

    @Test
    fun finishSessionSucceedsWhenProgramIsGone() = runTest {
        // Программу удалили физически (сессий на момент удаления ещё не было) — прогрессии просто нет.
        val repository = repository()
        val sessionId = repository.startSession(sampleWorkout())

        repository.finishSession(sessionId)

        assertNull(repository.getActiveSession())
    }

    @Test
    fun getActiveSessionIsNullBeforeStartAndAfterFinish() = runTest {
        val repository = repository()
        assertNull(repository.getActiveSession())

        val sessionId = repository.startSession(sampleWorkout())
        assertEquals(sessionId, repository.getActiveSession()?.id)

        repository.finishSession(sessionId)
        assertNull(repository.getActiveSession())
    }

    @Test
    fun getFinishedSessionsReportsSetCounts() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)
        val sessionId = repository.startSession(sampleWorkout())
        val setId = firstSetId(repository)
        repository.completeSet(setId = setId, count = 10, weight = 40.0)
        clock.nowMillis = FINISH_MILLIS
        repository.finishSession(sessionId)

        val finished = repository.getFinishedSessions()

        assertEquals(1, finished.size)
        val item = finished.single()
        assertEquals(sessionId, item.id)
        assertEquals("prog-1", item.program.id)
        // Список истории читает маркер из снапшота сессии, а не join'ом к программе.
        assertEquals(ExerciseAccent.Indigo, item.program.accent)
        assertEquals(ExerciseGlyph.BenchPress, item.program.glyph)
        assertEquals(3, item.totalSetCount)
        // Один выполнен, два пропущены завершением.
        assertEquals(1, item.completedSetCount)
        assertEquals(Instant.fromEpochMilliseconds(FINISH_MILLIS), item.finishedAt)
    }

    private fun repository(
        clock: Clock = MutableClock(START_MILLIS),
        workoutRepository: WorkoutRepository = FakeWorkoutRepository(),
    ): WorkoutSessionRepositoryImpl {
        // Упражнения существуют в библиотеке до старта сессии — session_exercise берёт имя оттуда.
        val dao = FakeWorkoutSessionDao().apply {
            exerciseLibrary["ex-1"] = libraryExercise(
                id = "ex-1",
                name = "Жим",
                description = "Грудь",
                accent = ExerciseAccent.Teal.key,
                glyph = ExerciseGlyph.PullUp.key,
            )
            exerciseLibrary["ex-2"] = libraryExercise(id = "ex-2", name = "Тяга", description = null)
        }
        return WorkoutSessionRepositoryImpl(
            workoutSessionDao = dao,
            workoutRepository = workoutRepository,
            clock = clock,
        )
    }

    private fun libraryExercise(
        id: String,
        name: String,
        description: String?,
        accent: String = ExerciseAccent.Default.key,
        glyph: String = ExerciseGlyph.Default.key,
    ): ExerciseDatabaseEntity =
        ExerciseDatabaseEntity(
            id = id,
            name = name,
            nameNormalized = name.lowercase(),
            description = description,
            accent = accent,
            glyph = glyph,
        )

    private suspend fun firstSetId(repository: WorkoutSessionRepositoryImpl): String =
        repository.getActiveSession()!!.exercises.first().sets.first().id

    private suspend fun setById(repository: WorkoutSessionRepositoryImpl, setId: String) =
        repository.getActiveSession()!!.exercises.flatMap { it.sets }.single { it.id == setId }

    private fun sampleWorkout(id: String = "prog-1"): WorkoutEntity = WorkoutEntity(
        id = id,
        name = "Push Day",
        description = "Толчковый день",
        accent = ExerciseAccent.Indigo,
        glyph = ExerciseGlyph.BenchPress,
        exercises = listOf(
            WorkoutExerciseWithRepsEntity(
                exercise = WorkoutExerciseEntity(id = "ex-1", name = "Жим", description = "Грудь"),
                reps = listOf(
                    WorkoutRepEntity(count = 10, weight = 40.0),
                    WorkoutRepEntity(count = 8, weight = 45.0),
                ),
            ),
            WorkoutExerciseWithRepsEntity(
                exercise = WorkoutExerciseEntity(id = "ex-2", name = "Тяга", description = null),
                reps = listOf(
                    WorkoutRepEntity(count = 12, weight = null),
                ),
            ),
        ),
    )

    private class MutableClock(var nowMillis: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(nowMillis)
    }

    private companion object {
        const val START_MILLIS = 1_000_000L
        const val FINISH_MILLIS = 4_000_000L
        const val LATER_MILLIS = 9_000_000L
    }
}
