package com.nikolaevskii.lyte.feature.splash.data.seed

import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity

/**
 * Стартовая библиотека упражнений — сеется в БД при первом запуске, см. [com.nikolaevskii.lyte.feature.splash.data.initializer.ExerciseLibraryInitializer].
 *
 * Маркеры (цвет и знак) — по макету v2 (`design/v2/LyteScreen.dc.html`, строки 557–575). Те же
 * значения раздаёт существующим установкам миграция БД v1→v2 (`Migration1To2` в `:core:core-db`),
 * адресуя строки по этим же `seed-*` id: у пользователя, поставившего v1, библиотека не должна
 * остаться серой. Меняешь маркер здесь — поменяй и там.
 */
internal object DefaultExerciseLibrary {

    val exercises: List<WorkoutExerciseEntity> = listOf(
        WorkoutExerciseEntity(
            id = "seed-back-squat",
            name = "Приседания со штангой",
            description = "Штанга на верхней части спины, присед до параллели бёдер с полом.",
            accent = ExerciseAccent.Lime,
            glyph = ExerciseGlyph.Squat,
        ),
        WorkoutExerciseEntity(
            id = "seed-deadlift",
            name = "Становая тяга",
            description = "Подъём штанги с пола за счёт разгибания бёдер и спины, руки прямые.",
            accent = ExerciseAccent.Coral,
            glyph = ExerciseGlyph.Deadlift,
        ),
        WorkoutExerciseEntity(
            id = "seed-bench-press",
            name = "Жим лёжа",
            description = "Жим штанги от середины груди лёжа на горизонтальной скамье.",
            accent = ExerciseAccent.Indigo,
            glyph = ExerciseGlyph.BenchPress,
        ),
        WorkoutExerciseEntity(
            id = "seed-bent-over-row",
            name = "Тяга штанги в наклоне",
            description = "Тяга штанги к поясу в наклоне, спина прямая, лопатки сводятся.",
            accent = ExerciseAccent.Coral,
            glyph = ExerciseGlyph.Deadlift,
        ),
        WorkoutExerciseEntity(
            id = "seed-pull-up",
            name = "Подтягивания",
            description = "Подъём тела к перекладине хватом сверху, до подбородка над грифом.",
            accent = ExerciseAccent.Coral,
            glyph = ExerciseGlyph.PullUp,
        ),
        WorkoutExerciseEntity(
            id = "seed-overhead-press",
            name = "Жим стоя",
            description = "Вертикальный жим штанги над головой стоя, корпус зафиксирован.",
            accent = ExerciseAccent.Indigo,
            glyph = ExerciseGlyph.DumbbellPress,
        ),
        WorkoutExerciseEntity(
            id = "seed-biceps-curl",
            name = "Сгибания на бицепс",
            description = "Сгибание рук с гантелями, локти прижаты к корпусу.",
            accent = ExerciseAccent.Amber,
            glyph = ExerciseGlyph.Curl,
        ),
        WorkoutExerciseEntity(
            id = "seed-incline-dumbbell-press",
            name = "Жим гантелей на наклонной",
            description = "Жим гантелей от верха груди на скамье с наклоном 30–45°.",
            accent = ExerciseAccent.Indigo,
            glyph = ExerciseGlyph.DumbbellPress,
        ),
        WorkoutExerciseEntity(
            id = "seed-dip",
            name = "Отжимания на брусьях",
            description = "Опускание и подъём тела на брусьях, наклон вперёд — акцент на грудь.",
            accent = ExerciseAccent.Teal,
            glyph = ExerciseGlyph.PullUp,
        ),
        WorkoutExerciseEntity(
            id = "seed-triceps-pushdown",
            name = "Разгибания на блоке",
            description = "Разгибание рук на верхнем блоке вниз, локти неподвижны.",
            accent = ExerciseAccent.Amber,
            glyph = ExerciseGlyph.Machine,
        ),
    )
}
