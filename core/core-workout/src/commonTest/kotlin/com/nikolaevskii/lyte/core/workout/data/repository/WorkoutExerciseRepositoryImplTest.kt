package com.nikolaevskii.lyte.core.workout.data.repository

import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Записан явно: в тексте теста неразрывный пробел не отличить от обычного. */
private const val NON_BREAKING_SPACE = "\u00A0"

class WorkoutExerciseRepositoryImplTest {

    @Test
    fun createThenGetReturnsExercise() = runTest {
        val repository = repository()
        val exercise = WorkoutExerciseEntity(
            id = "ex-1",
            name = "Жим",
            description = "Грудь",
            // Маркер отличен от дефолтного — сравнение целиком доказывает, что он доезжает до БД и обратно.
            accent = ExerciseAccent.Indigo,
            glyph = ExerciseGlyph.BenchPress,
        )

        repository.createExercise(exercise)

        assertEquals(exercise, repository.getExercise("ex-1"))
    }

    @Test
    fun exerciseWithoutChosenMarkerGetsDefaultOne() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим"))

        val loaded = repository.getExercise("ex-1")

        assertEquals(ExerciseAccent.Slate, loaded?.accent)
        assertEquals(ExerciseGlyph.Squat, loaded?.glyph)
    }

    @Test
    fun getExercisesReturnsAll() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-2", name = "Тяга"))

        assertEquals(2, repository.getExercises().size)
    }

    @Test
    fun getExercisesSortsByNameAscendingIgnoringCase() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Тяга штанги в наклоне"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-2", name = "приседания со штангой"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-3", name = "Жим лёжа"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-4", name = "Bench Press"))

        val names = repository.getExercises().map { exercise -> exercise.name }

        assertEquals(listOf("Bench Press", "Жим лёжа", "приседания со штангой", "Тяга штанги в наклоне"), names)
    }

    @Test
    fun getExercisesFiltersByNameIgnoringCaseAndSurroundingSpaces() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим лёжа"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-2", name = "жим стоя"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-3", name = "Приседания со штангой"))

        val names = repository.getExercises(query = "  ЖиМ  ").map { exercise -> exercise.name }

        assertEquals(listOf("Жим лёжа", "жим стоя"), names)
    }

    /**
     * Неразрывный пробел внутри имени подставляют клавиатуры (в том числе штатная iOS). Совпасть
     * с обычным пробелом он обязан в обе стороны, иначе упражнение перестаёт находиться ровно тем
     * запросом, который перекрывает эту позицию.
     */
    @Test
    fun getExercisesMatchesAcrossNonBreakingSpace() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим${NON_BREAKING_SPACE}лёжа"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-2", name = "Тяга штанги"))

        assertEquals(listOf("Жим${NON_BREAKING_SPACE}лёжа"), repository.getExercises(query = "жим л").map { it.name })
        assertEquals(listOf("Тяга штанги"), repository.getExercises(query = "тяга${NON_BREAKING_SPACE}шт").map { it.name })
    }

    @Test
    fun getExercisesMatchesSubstringInTheMiddleOfTheName() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Тяга штанги в наклоне"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-2", name = "Приседания со штангой"))

        assertEquals(2, repository.getExercises(query = "штанг").size)
    }

    @Test
    fun getExercisesWithoutMatchesReturnsEmptyList() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим лёжа"))

        assertEquals(emptyList(), repository.getExercises(query = "Жим Арнольда"))
    }

    @Test
    fun likeWildcardsInQueryAreTreatedAsPlainCharacters() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим лёжа"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-2", name = "Приседания 100%"))

        // Без экранирования «%» совпал бы со всем, а «_» — с любым одиночным символом.
        assertEquals(listOf("Приседания 100%"), repository.getExercises(query = "%").map { it.name })
        assertEquals(emptyList(), repository.getExercises(query = "_"))
    }

    @Test
    fun editExercisesUpdatesInPlace() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим"))

        repository.editExercises(
            WorkoutExerciseEntity(id = "ex-1", name = "Жим лёжа", description = "Грудь"),
        )

        val loaded = repository.getExercise("ex-1")
        assertEquals("Жим лёжа", loaded?.name)
        assertEquals("Грудь", loaded?.description)
        assertEquals(1, repository.getExercises().size)
    }

    @Test
    fun editExercisesKeepsNameSearchableAfterRename() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим"))

        repository.editExercises(WorkoutExerciseEntity(id = "ex-1", name = "Приседания", description = null))

        assertEquals(emptyList(), repository.getExercises(query = "жим"))
        assertEquals(1, repository.getExercises(query = "приседания").size)
    }

    @Test
    fun deleteExerciseWithoutReferencesRemovesIt() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим"))

        repository.deleteExercise("ex-1")

        assertNull(repository.getExercise("ex-1"))
    }

    @Test
    fun deleteExerciseWithReferencesArchivesItInsteadOfDeleting() = runTest {
        val dao = FakeExerciseDao()
        val repository = WorkoutExerciseRepositoryImpl(exerciseDao = dao)
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим", description = "Грудь"))
        // На упражнение ссылается программа/сессия — физическое удаление запрещено.
        dao.referenceCountByExercise["ex-1"] = 1

        repository.deleteExercise("ex-1")

        // Упражнение скрыто из библиотеки, но остаётся доступным по id (ссылки и история целы).
        assertEquals(emptyList(), repository.getExercises(query = "жим"))
        assertEquals("Жим", repository.getExercise("ex-1")?.name)
    }

    private fun repository(): WorkoutExerciseRepositoryImpl =
        WorkoutExerciseRepositoryImpl(exerciseDao = FakeExerciseDao())
}
