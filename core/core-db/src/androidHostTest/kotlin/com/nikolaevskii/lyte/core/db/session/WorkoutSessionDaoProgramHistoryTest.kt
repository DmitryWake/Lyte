package com.nikolaevskii.lyte.core.db.session

import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.nikolaevskii.lyte.core.db.LyteDatabase
import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Предикаты и порядок [WorkoutSessionDao.getProgramSetHistory] на настоящей SQLite. Фейк DAO в
 * `:core:core-session` повторяет их вручную и доказать не может: убери из фейка фильтр по программе —
 * гейт останется зелёным, а в карточке подхода поедут цифры из чужой тренировки.
 *
 * БД поднимается **настоящим** Room-билдером in-memory, а не сырым драйвером, как в
 * `Migration1To2Test`: проверять надо тот же сгенерированный код, который работает в приложении.
 * Драйвер при этом [AndroidSQLiteDriver] под Robolectric, а не `applyLyteDefaults()`: тот ставит
 * `BundledSQLiteDriver`, чей JNI собран под Android и на host-JVM не грузится
 * (`UnsatisfiedLinkError`). SQL от выбора драйвера не зависит, поэтому проверка на Android покрывает
 * и iOS.
 */
@RunWith(RobolectricTestRunner::class)
class WorkoutSessionDaoProgramHistoryTest {

    @Test
    fun returnsSetsOfRequestedProgramOnly() = withDao { dao ->
        dao.insertSessionWithOneSet(sessionId = "own", programId = PROGRAM_ID, finishedAt = 1_000)
        dao.insertSessionWithOneSet(sessionId = "alien", programId = "prog-alien", finishedAt = 2_000)

        val rows = dao.getProgramSetHistory(PROGRAM_ID)

        assertEquals(listOf("own$SET_ID_SUFFIX"), rows.map { row -> row.set.id })
    }

    @Test
    fun ignoresUnfinishedSession() = withDao { dao ->
        dao.insertSessionWithOneSet(sessionId = "finished", programId = PROGRAM_ID, finishedAt = 1_000)
        dao.insertSessionWithOneSet(sessionId = "active", programId = PROGRAM_ID, finishedAt = null)

        val rows = dao.getProgramSetHistory(PROGRAM_ID)

        assertEquals(listOf("finished$SET_ID_SUFFIX"), rows.map { row -> row.set.id })
    }

    @Test
    fun returnsFinishedSessionsNewestFirst() = withDao { dao ->
        // Старая сессия вставлена первой намеренно: без ORDER BY строки вернулись бы в порядке
        // вставки, и тест обязан отличить сортировку от совпадения.
        dao.insertSessionWithOneSet(sessionId = "older", programId = PROGRAM_ID, finishedAt = 1_000)
        dao.insertSessionWithOneSet(sessionId = "newer", programId = PROGRAM_ID, finishedAt = 2_000)

        val rows = dao.getProgramSetHistory(PROGRAM_ID)

        assertEquals(listOf("newer", "older"), rows.map { row -> row.sessionId })
    }

    /**
     * Поднимает БД в памяти, кладёт упражнение библиотеки (без него FK `session_exercise.exercise_id`
     * не пустит ни одной сессии) и отдаёт DAO тесту. Закрывается в любом случае — иначе упавший тест
     * утащил бы за собой соседей.
     */
    private fun withDao(block: suspend (WorkoutSessionDao) -> Unit) = runTest {
        val database = Room
            .inMemoryDatabaseBuilder<LyteDatabase>(context = RuntimeEnvironment.getApplication())
            .setDriver(AndroidSQLiteDriver())
            .build()
        try {
            database.exerciseDao().upsert(
                ExerciseDatabaseEntity(
                    id = EXERCISE_ID,
                    name = "Жим",
                    nameNormalized = "жим",
                    description = null,
                    accent = ACCENT,
                    glyph = GLYPH,
                ),
            )
            block(database.workoutSessionDao())
        } finally {
            database.close()
        }
    }

    /**
     * Кладёт сессию из одного упражнения с одним выполненным подходом. Id упражнения сессии и
     * подхода выводятся из [sessionId], чтобы утверждение читалось без справочника.
     */
    private suspend fun WorkoutSessionDao.insertSessionWithOneSet(
        sessionId: String,
        programId: String,
        finishedAt: Long?,
    ) {
        insertSession(
            WorkoutSessionDatabaseEntity(
                id = sessionId,
                programId = programId,
                programName = "Push Day",
                programAccent = ACCENT,
                programGlyph = GLYPH,
                startedAt = 0,
                finishedAt = finishedAt,
                currentExerciseId = null,
            ),
        )
        insertExercises(
            listOf(
                SessionExerciseDatabaseEntity(
                    id = "$sessionId$EXERCISE_ID_SUFFIX",
                    sessionId = sessionId,
                    exerciseId = EXERCISE_ID,
                    position = 0,
                ),
            ),
        )
        insertSets(
            listOf(
                SessionSetDatabaseEntity(
                    id = "$sessionId$SET_ID_SUFFIX",
                    sessionExerciseId = "$sessionId$EXERCISE_ID_SUFFIX",
                    position = 0,
                    targetCount = 10,
                    targetWeight = 40.0,
                    resultStatus = SessionSetDatabaseEntity.RESULT_STATUS_COMPLETED,
                    resultCount = 10,
                    resultWeight = 40.0,
                    note = "",
                ),
            ),
        )
    }

    private companion object {

        const val PROGRAM_ID = "prog-1"
        const val EXERCISE_ID = "ex-1"
        const val EXERCISE_ID_SUFFIX = "-exercise"
        const val SET_ID_SUFFIX = "-set"

        // Маркер к запросу отношения не имеет, но колонки NOT NULL — берём дефолтные ключи.
        const val ACCENT = "slate"
        const val GLYPH = "squat"
    }
}
