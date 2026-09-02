package com.nikolaevskii.lyte.core.session.data.repository

import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetOutcomeEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
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
    fun getFinishedSessionsReportsSetOutcomesInSessionOrder() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)
        val sessionId = repository.startSession(sampleWorkout())
        val setIds = setIds(repository)
        // Первый подход ровно в цель, второй с перевыполнением, третий остаётся незакрытым.
        repository.completeSet(setId = setIds[0], count = 10, weight = 40.0)
        repository.completeSet(setId = setIds[1], count = 9, weight = 45.0)
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
        // Порядок исходов — порядок сессии: два подхода первого упражнения, затем подход второго.
        assertEquals(
            listOf(
                SessionSetOutcomeEntity.MET,
                SessionSetOutcomeEntity.EXCEEDED,
                SessionSetOutcomeEntity.SKIPPED,
            ),
            item.setOutcomes,
        )
        assertEquals(Instant.fromEpochMilliseconds(FINISH_MILLIS), item.finishedAt)
    }

    @Test
    fun getFinishedSessionsSeparatesOutcomesOfDifferentSessions() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)

        val firstId = repository.startSession(sampleWorkout())
        repository.completeSet(setId = setIds(repository)[0], count = 10, weight = 40.0)
        clock.nowMillis = FINISH_MILLIS
        repository.finishSession(firstId)

        val secondId = repository.startSession(emptyWorkout())
        clock.nowMillis = LATER_MILLIS
        repository.finishSession(secondId)

        val finished = repository.getFinishedSessions()

        assertEquals(listOf(secondId, firstId), finished.map { it.id })
        // Сессия без подходов не наследует чужие исходы — у неё пустой трек.
        assertEquals(emptyList(), finished.first().setOutcomes)
        assertEquals(3, finished.last().setOutcomes.size)
    }

    @Test
    fun deleteSessionRemovesItWithSetsAndLeavesNeighbourAndProgramIntact() = runTest {
        val clock = MutableClock(START_MILLIS)
        val workoutRepository = FakeWorkoutRepository().apply { createWorkout(sampleWorkout()) }
        val repository = repository(clock = clock, workoutRepository = workoutRepository)

        val firstId = repository.startSession(sampleWorkout())
        clock.nowMillis = FINISH_MILLIS
        repository.finishSession(firstId)
        val secondId = repository.startSession(sampleWorkout())
        clock.nowMillis = LATER_MILLIS
        repository.finishSession(secondId)

        repository.deleteSession(firstId)

        // Сессия исчезла вместе со своими подходами; соседняя осталась целой.
        assertNull(repository.getSession(firstId))
        assertEquals(listOf(secondId), repository.getFinishedSessions().map { it.id })
        assertEquals(3, repository.getFinishedSessions().single().setOutcomes.size)
        // Программа удалением сессии не задета: FK на workout у сессии нет.
        assertEquals(sampleWorkout(), workoutRepository.getWorkout("prog-1"))
    }

    @Test
    fun previousSetResultsComeFromTheLatestFinishedSession() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)
        runFinishedSession(
            repository = repository,
            workout = sampleWorkout(),
            results = mapOf(0 to fact(count = 10, weight = 40.0), 1 to fact(count = 8, weight = 45.0)),
        )
        clock.nowMillis = LATER_MILLIS
        runFinishedSession(
            repository = repository,
            workout = sampleWorkout(),
            results = mapOf(0 to fact(count = 11, weight = 42.5), 1 to fact(count = 9, weight = 47.5)),
        )
        val active = startActiveSession(repository = repository, workout = sampleWorkout())

        val previous = repository.getPreviousSetResults(active)

        val sets = active.exercises.first().sets
        assertEquals(fact(count = 11, weight = 42.5), previous[sets[0].id])
        assertEquals(fact(count = 9, weight = 47.5), previous[sets[1].id])
    }

    @Test
    fun previousSetResultsLookDeeperForSetSkippedInTheLatestSession() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)
        runFinishedSession(
            repository = repository,
            workout = sampleWorkout(),
            results = mapOf(0 to fact(count = 10, weight = 40.0), 1 to fact(count = 8, weight = 45.0)),
        )
        clock.nowMillis = LATER_MILLIS
        // Второй подход остался незакрытым — завершение пометило его SKIPPED, как при досрочном.
        runFinishedSession(
            repository = repository,
            workout = sampleWorkout(),
            results = mapOf(0 to fact(count = 11, weight = 42.5), 2 to fact(count = 13, weight = null)),
        )
        val active = startActiveSession(repository = repository, workout = sampleWorkout())

        val previous = repository.getPreviousSetResults(active)

        val sets = active.exercises.flatMap { exercise -> exercise.sets }
        // Пропущенный подход берёт факт из сессии, где его делали; соседние остаются из последней.
        assertEquals(fact(count = 11, weight = 42.5), previous[sets[0].id])
        assertEquals(fact(count = 8, weight = 45.0), previous[sets[1].id])
        assertEquals(fact(count = 13, weight = null), previous[sets[2].id])
    }

    @Test
    fun previousSetResultsDistinguishOccurrencesOfTheSameExercise() = runTest {
        val repository = repository()
        val workout = repeatedExerciseWorkout()
        runFinishedSession(
            repository = repository,
            workout = workout,
            results = mapOf(
                0 to fact(count = 10, weight = 40.0),
                1 to fact(count = 12, weight = null),
                2 to fact(count = 5, weight = 52.5),
            ),
        )
        val active = startActiveSession(repository = repository, workout = workout)

        val previous = repository.getPreviousSetResults(active)

        // Оба вхождения — одно упражнение библиотеки, но факты у них свои.
        val sets = active.exercises.flatMap { exercise -> exercise.sets }
        assertEquals(fact(count = 10, weight = 40.0), previous[sets[0].id])
        assertEquals(fact(count = 12, weight = null), previous[sets[1].id])
        assertEquals(fact(count = 5, weight = 52.5), previous[sets[2].id])
    }

    @Test
    fun previousSetResultsKeepOccurrenceNumberingWhenAnOccurrenceWasSkipped() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)
        val workout = repeatedExerciseWorkout()
        runFinishedSession(
            repository = repository,
            workout = workout,
            results = mapOf(
                0 to fact(count = 10, weight = 40.0),
                1 to fact(count = 12, weight = null),
                2 to fact(count = 5, weight = 52.5),
            ),
        )
        clock.nowMillis = LATER_MILLIS
        // В свежей сессии первое вхождение жима пропущено целиком: нумерация вхождений не должна
        // съехать, иначе факты второго вхождения подставятся первому.
        runFinishedSession(
            repository = repository,
            workout = workout,
            results = mapOf(1 to fact(count = 13, weight = null), 2 to fact(count = 6, weight = 55.0)),
        )
        val active = startActiveSession(repository = repository, workout = workout)

        val previous = repository.getPreviousSetResults(active)

        val sets = active.exercises.flatMap { exercise -> exercise.sets }
        assertEquals(fact(count = 10, weight = 40.0), previous[sets[0].id])
        assertEquals(fact(count = 13, weight = null), previous[sets[1].id])
        assertEquals(fact(count = 6, weight = 55.0), previous[sets[2].id])
    }

    @Test
    fun previousSetResultsSurviveExerciseInsertedIntoProgram() = runTest {
        val repository = repository()
        runFinishedSession(
            repository = repository,
            workout = programOf(listOf(benchPressExercise())),
            results = mapOf(0 to fact(count = 10, weight = 40.0), 1 to fact(count = 8, weight = 45.0)),
        )
        // Программу отредактировали: в начало вставлено упражнение, жим съехал на позицию ниже.
        val edited = programOf(listOf(rowExercise(), benchPressExercise()))
        val active = startActiveSession(repository = repository, workout = edited)

        val previous = repository.getPreviousSetResults(active)

        // Ориентиры жима на месте — сопоставление по вхождению упражнения, а не по его позиции.
        val sets = active.exercises.flatMap { exercise -> exercise.sets }
        assertNull(previous[sets[0].id])
        assertEquals(fact(count = 10, weight = 40.0), previous[sets[1].id])
        assertEquals(fact(count = 8, weight = 45.0), previous[sets[2].id])
    }

    @Test
    fun previousSetResultsAreEmptyWithoutHistory() = runTest {
        val repository = repository()
        val active = startActiveSession(repository = repository, workout = sampleWorkout())

        assertEquals(emptyMap(), repository.getPreviousSetResults(active))
    }

    @Test
    fun previousSetResultsIgnoreUnfinishedSession() = runTest {
        val clock = MutableClock(START_MILLIS)
        val repository = repository(clock)
        runFinishedSession(
            repository = repository,
            workout = sampleWorkout(),
            results = mapOf(0 to fact(count = 10, weight = 40.0)),
        )
        clock.nowMillis = LATER_MILLIS
        val active = startActiveSession(repository = repository, workout = sampleWorkout())
        repository.completeSet(setId = active.exercises.first().sets.first().id, count = 99, weight = 99.0)

        val previous = repository.getPreviousSetResults(repository.getActiveSession()!!)

        // Факт текущей сессии сам себе ориентиром не становится: она ещё не завершена.
        assertEquals(fact(count = 10, weight = 40.0), previous[active.exercises.first().sets.first().id])
    }

    @Test
    fun previousSetResultsHaveNoEntryForSetAddedAfterTheLastSession() = runTest {
        val repository = repository()
        runFinishedSession(
            repository = repository,
            workout = programOf(listOf(benchPressExercise())),
            results = mapOf(0 to fact(count = 10, weight = 40.0), 1 to fact(count = 8, weight = 45.0)),
        )
        // Программу отредактировали: у жима стало четыре подхода вместо двух.
        val extended = programOf(
            listOf(
                exerciseWithReps(
                    id = "ex-1",
                    name = "Жим",
                    reps = listOf(10 to 40.0, 8 to 45.0, 6 to 50.0, 4 to 55.0),
                ),
            ),
        )
        val active = startActiveSession(repository = repository, workout = extended)

        val previous = repository.getPreviousSetResults(active)

        val sets = active.exercises.single().sets
        assertEquals(fact(count = 10, weight = 40.0), previous[sets[0].id])
        assertEquals(fact(count = 8, weight = 45.0), previous[sets[1].id])
        // Позиции, которой в прошлой сессии не было, ориентир не выдумывается — ни нулём, ни соседом.
        assertNull(previous[sets[2].id])
        assertNull(previous[sets[3].id])
    }

    @Test
    fun skipSetErasesRecordedResult() = runTest {
        val dao = sessionDao()
        val repository = repository(dao = dao)
        val sessionId = repository.startSession(sampleWorkout())
        val setId = firstSetId(repository)
        repository.completeSet(setId = setId, count = 10, weight = 40.0)

        repository.skipSet(setId)

        assertEquals(SessionSetResultEntity.Skipped, setById(repository, setId).result)
        // Проверяется строка, а не доменная модель: `Skipped` значения не несёт, и стёрлось ли
        // записанное число, по ней не видно. А правило «пропущенный подход не ориентир» держится
        // именно на пустом result_count — доменная модель скрыла бы недосмотр.
        val row = dao.getSession(sessionId)!!.exercises.flatMap { it.sets }.single { it.id == setId }
        assertNull(row.resultCount)
        assertNull(row.resultWeight)
    }

    private fun repository(
        clock: Clock = MutableClock(START_MILLIS),
        workoutRepository: WorkoutRepository = FakeWorkoutRepository(),
        dao: FakeWorkoutSessionDao = sessionDao(),
    ): WorkoutSessionRepositoryImpl =
        WorkoutSessionRepositoryImpl(
            workoutSessionDao = dao,
            workoutRepository = workoutRepository,
            clock = clock,
        )

    /** DAO с готовой библиотекой: упражнения существуют до старта сессии — session_exercise берёт имя оттуда. */
    private fun sessionDao(): FakeWorkoutSessionDao = FakeWorkoutSessionDao().apply {
        exerciseLibrary["ex-1"] = libraryExercise(
            id = "ex-1",
            name = "Жим",
            description = "Грудь",
            accent = ExerciseAccent.Teal.key,
            glyph = ExerciseGlyph.PullUp.key,
        )
        exerciseLibrary["ex-2"] = libraryExercise(id = "ex-2", name = "Тяга", description = null)
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

    /** Id всех подходов активной сессии в её порядке: упражнение → подход. */
    private suspend fun setIds(repository: WorkoutSessionRepositoryImpl): List<String> =
        repository.getActiveSession()!!.exercises.flatMap { exercise -> exercise.sets.map { it.id } }

    private suspend fun setById(repository: WorkoutSessionRepositoryImpl, setId: String) =
        repository.getActiveSession()!!.exercises.flatMap { it.sets }.single { it.id == setId }

    /**
     * Проводит завершённую сессию по [workout]: старт, факты в подходах с индексами из [results]
     * (индекс — порядок сессии: упражнение → подход), завершение. Подходы вне [results] остаются
     * незакрытыми и уходят в `SKIPPED` — ровно так их помечает завершение в приложении.
     *
     * Время старта и завершения берётся из текущего значения часов: чтобы отличить сессии по
     * свежести, часы двигает вызывающий между вызовами.
     */
    private suspend fun runFinishedSession(
        repository: WorkoutSessionRepositoryImpl,
        workout: WorkoutEntity,
        results: Map<Int, SessionSetValueEntity>,
    ) {
        val sessionId = repository.startSession(workout)
        val setIds = setIds(repository)
        results.forEach { (index, value) ->
            repository.completeSet(setId = setIds[index], count = value.count, weight = value.weight)
        }
        repository.finishSession(sessionId)
    }

    private suspend fun startActiveSession(
        repository: WorkoutSessionRepositoryImpl,
        workout: WorkoutEntity,
    ): WorkoutSessionEntity {
        repository.startSession(workout)
        return repository.getActiveSession()!!
    }

    private fun fact(count: Int, weight: Double?): SessionSetValueEntity =
        SessionSetValueEntity(count = count, weight = weight)

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

    private fun emptyWorkout(): WorkoutEntity = WorkoutEntity(
        id = "prog-empty",
        name = "Пустая",
        description = null,
        exercises = emptyList(),
    )

    /** Программа с тем же id, что [sampleWorkout], но своим составом: её правили между сессиями. */
    private fun programOf(exercises: List<WorkoutExerciseWithRepsEntity>): WorkoutEntity = WorkoutEntity(
        id = "prog-1",
        name = "Push Day",
        description = "Толчковый день",
        accent = ExerciseAccent.Indigo,
        glyph = ExerciseGlyph.BenchPress,
        exercises = exercises,
    )

    /** Программа, где одно упражнение библиотеки стоит дважды — между вхождениями другое. */
    private fun repeatedExerciseWorkout(): WorkoutEntity = programOf(
        listOf(
            exerciseWithReps(id = "ex-1", name = "Жим", reps = listOf(10 to 40.0)),
            rowExercise(),
            exerciseWithReps(id = "ex-1", name = "Жим", reps = listOf(6 to 50.0)),
        ),
    )

    private fun benchPressExercise(): WorkoutExerciseWithRepsEntity =
        exerciseWithReps(id = "ex-1", name = "Жим", reps = listOf(10 to 40.0, 8 to 45.0))

    private fun rowExercise(): WorkoutExerciseWithRepsEntity =
        exerciseWithReps(id = "ex-2", name = "Тяга", reps = listOf(12 to null))

    private fun exerciseWithReps(
        id: String,
        name: String,
        reps: List<Pair<Int, Double?>>,
    ): WorkoutExerciseWithRepsEntity = WorkoutExerciseWithRepsEntity(
        exercise = WorkoutExerciseEntity(id = id, name = name),
        reps = reps.map { (count, weight) -> WorkoutRepEntity(count = count, weight = weight) },
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
