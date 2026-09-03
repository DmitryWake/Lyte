package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.nikolaevskii.lyte.core.db.LyteDatabase
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Soft-delete на настоящей SQLite: кого отдаёт [WorkoutDao.getWithExercises] и что делает с
 * `is_archived` [WorkoutDao.saveWorkoutGraph]. Фейк DAO в `:core:core-workout` повторяет обе
 * договорённости вручную и доказать их не может: убери из фейка фильтр — гейт останется зелёным,
 * а по удалённой программе снова запустится тренировка.
 *
 * БД поднимается **настоящим** Room-билдером in-memory, а не сырым драйвером, как в
 * `Migration1To2Test`: проверять надо тот же сгенерированный код, который работает в приложении
 * (и `PRAGMA foreign_keys`, без которой каскад `workout_exercise → workout_set` молчит). Драйвер
 * при этом [AndroidSQLiteDriver] под Robolectric, а не `applyLyteDefaults()`: тот ставит
 * `BundledSQLiteDriver`, чей JNI собран под Android и на host-JVM не грузится
 * (`UnsatisfiedLinkError`). SQL от выбора драйвера не зависит, поэтому проверка на Android
 * покрывает и iOS.
 */
@RunWith(RobolectricTestRunner::class)
class WorkoutDaoArchiveTest {

    @Test
    fun returnsLiveProgramWithItsExercises() = withDatabase { database ->
        database.workoutDao().saveProgram()

        val loaded = database.workoutDao().getWithExercises(WORKOUT_ID)

        assertNotNull(loaded)
        assertEquals(listOf(EXERCISE_ID), loaded.exercises.map { row -> row.exercise.id })
    }

    @Test
    fun skipsArchivedProgram() = withDatabase { database ->
        database.workoutDao().saveProgram()
        database.workoutDao().archiveWorkout(WORKOUT_ID)

        assertNull(database.workoutDao().getWithExercises(WORKOUT_ID))
    }

    @Test
    fun keepsArchivedExerciseInProgramGraph() = withDatabase { database ->
        database.workoutDao().saveProgram()
        // Упражнение удалили из библиотеки, но оно осталось в программе — фильтр по архиву снаружи
        // графа, иначе состав программы молча поредел бы.
        database.exerciseDao().archiveExercise(EXERCISE_ID)

        val loaded = database.workoutDao().getWithExercises(WORKOUT_ID)

        assertNotNull(loaded)
        assertEquals(listOf(EXERCISE_ID), loaded.exercises.map { row -> row.exercise.id })
    }

    @Test
    fun saveKeepsProgramArchived() = withDatabase { database ->
        database.workoutDao().saveProgram()
        database.workoutDao().archiveWorkout(WORKOUT_ID)

        // Маппер про архив не знает и приносит is_archived = 0 — как при обычном сохранении из редактора.
        database.workoutDao().saveProgram()

        assertTrue(database.workoutDao().isWorkoutArchived(WORKOUT_ID))
        assertNull(database.workoutDao().getWithExercises(WORKOUT_ID))
    }

    @Test
    fun saveKeepsExerciseArchived() = withDatabase { database ->
        database.workoutDao().saveProgram()
        database.exerciseDao().archiveExercise(EXERCISE_ID)

        database.workoutDao().saveProgram()

        assertTrue(database.exerciseDao().getById(EXERCISE_ID)?.isArchived == true)
        assertTrue(database.exerciseDao().search(normalizedQuery = "").isEmpty())
    }

    @Test
    fun saveInsertsRowsUnarchived() = withDatabase { database ->
        database.workoutDao().saveProgram()

        // Перенос флага не должен архивировать то, чего в БД ещё не было.
        assertFalse(database.workoutDao().isWorkoutArchived(WORKOUT_ID))
        assertEquals(listOf(EXERCISE_ID), database.exerciseDao().search(normalizedQuery = "").map { it.id })
    }

    /**
     * Поднимает БД в памяти и отдаёт её тесту. Закрывается в любом случае — иначе упавший тест
     * утащил бы за собой соседей.
     */
    private fun withDatabase(block: suspend (LyteDatabase) -> Unit) = runTest {
        val database = Room
            .inMemoryDatabaseBuilder<LyteDatabase>(context = RuntimeEnvironment.getApplication())
            .setDriver(AndroidSQLiteDriver())
            .build()
        try {
            block(database)
        } finally {
            database.close()
        }
    }

    /** Программа из одного упражнения с одним подходом — тем же путём, каким её пишет редактор. */
    private suspend fun WorkoutDao.saveProgram() {
        saveWorkoutGraph(
            workout = WorkoutDatabaseEntity(
                id = WORKOUT_ID,
                name = "Push Day",
                description = null,
                accent = ACCENT,
                glyph = GLYPH,
            ),
            exercises = listOf(
                ExerciseDatabaseEntity(
                    id = EXERCISE_ID,
                    name = "Жим",
                    nameNormalized = "жим",
                    description = null,
                    accent = ACCENT,
                    glyph = GLYPH,
                ),
            ),
            crossRefs = listOf(
                WorkoutExerciseCrossRefDatabaseEntity(
                    id = CROSS_REF_ID,
                    workoutId = WORKOUT_ID,
                    exerciseId = EXERCISE_ID,
                    position = 0,
                ),
            ),
            sets = listOf(
                WorkoutSetDatabaseEntity(
                    id = SET_ID,
                    workoutExerciseId = CROSS_REF_ID,
                    position = 0,
                    count = 10,
                    weight = 40.0,
                ),
            ),
        )
    }

    private companion object {

        const val WORKOUT_ID = "prog-1"
        const val EXERCISE_ID = "ex-1"
        const val CROSS_REF_ID = "prog-1#0"
        const val SET_ID = "prog-1#0#0"

        // Маркер к архиву отношения не имеет, но колонки NOT NULL — берём дефолтные ключи.
        const val ACCENT = "slate"
        const val GLYPH = "squat"
    }
}
