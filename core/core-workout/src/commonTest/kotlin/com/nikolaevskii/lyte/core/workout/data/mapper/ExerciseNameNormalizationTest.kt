package com.nikolaevskii.lyte.core.workout.data.mapper

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** Записан явно: в тексте теста неразрывный пробел не отличить от обычного. */
private const val NBSP = "\u00A0"

class ExerciseNameNormalizationTest {

    @Test
    fun normalizationLowercasesAndUnbreaksSpaces() {
        assertEquals("жим лёжа", "Жим${NBSP}Лёжа".normalizedForSearch())
        assertEquals("жим лёжа", "Жим лёжа".normalizedForSearch())
    }

    /**
     * Имя показывается пользователю ровно таким, каким он его ввёл: неразрывный пробел — часть
     * набранного текста, а не грязь. Нормализация живёт только в служебной колонке.
     */
    @Test
    fun storedNameKeepsWhatUserTypedWhileNormalizedColumnDoesNot() {
        val name = "Жим${NBSP}лёжа"

        val stored = WorkoutExerciseEntity(id = "ex-1", name = name).toDatabaseEntity()

        assertEquals(name, stored.name)
        assertEquals("жим лёжа", stored.nameNormalized)
        assertFalse(stored.nameNormalized.contains(NBSP), "в name_normalized неразрывного пробела быть не должно")
    }
}
