package com.nikolaevskii.lyte.core.workout.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Ключи — формат хранения: их значения зафиксированы здесь, чтобы переименование значения enum'а
 * не переписало молча содержимое БД (и не потребовало миграции незамеченным).
 */
class ExerciseMarkerTest {

    @Test
    fun accentKeysAreStable() {
        assertEquals(
            expected = listOf("coral", "indigo", "lime", "amber", "teal", "slate"),
            actual = ExerciseAccent.entries.map { accent -> accent.key },
        )
    }

    @Test
    fun glyphKeysAreStable() {
        assertEquals(
            expected = listOf(
                "squat",
                "deadlift",
                "bench-press",
                "pull-up",
                "dumbbell-press",
                "curl",
                "crunch",
                "stretch",
                "rack",
                "machine",
            ),
            actual = ExerciseGlyph.entries.map { glyph -> glyph.key },
        )
    }

    @Test
    fun unknownKeyFallsBackToDefault() {
        assertEquals(ExerciseAccent.Slate, ExerciseAccent.fromKey("magenta"))
        assertEquals(ExerciseGlyph.Squat, ExerciseGlyph.fromKey("kettlebell"))
        // Чтение не должно зависеть от регистра случайно: ключ сравнивается точно.
        assertEquals(ExerciseAccent.Slate, ExerciseAccent.fromKey("CORAL"))
    }

    @Test
    fun knownKeyResolvesToItsValue() {
        assertEquals(ExerciseAccent.Lime, ExerciseAccent.fromKey("lime"))
        assertEquals(ExerciseGlyph.BenchPress, ExerciseGlyph.fromKey("bench-press"))
    }
}
