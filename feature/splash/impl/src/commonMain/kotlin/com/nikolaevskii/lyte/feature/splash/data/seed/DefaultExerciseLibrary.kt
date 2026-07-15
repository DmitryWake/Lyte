package com.nikolaevskii.lyte.feature.splash.data.seed

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity

/** Стартовая библиотека упражнений — сеется в БД при первом запуске, см. [com.nikolaevskii.lyte.feature.splash.data.initializer.ExerciseLibraryInitializer]. */
internal object DefaultExerciseLibrary {

    val exercises: List<WorkoutExerciseEntity> = listOf(
        WorkoutExerciseEntity(
            id = "seed-back-squat",
            name = "Приседания со штангой",
            description = "Штанга на верхней части спины, присед до параллели бёдер с полом.",
        ),
        WorkoutExerciseEntity(
            id = "seed-deadlift",
            name = "Становая тяга",
            description = "Подъём штанги с пола за счёт разгибания бёдер и спины, руки прямые.",
        ),
        WorkoutExerciseEntity(
            id = "seed-bench-press",
            name = "Жим лёжа",
            description = "Жим штанги от середины груди лёжа на горизонтальной скамье.",
        ),
        WorkoutExerciseEntity(
            id = "seed-bent-over-row",
            name = "Тяга штанги в наклоне",
            description = "Тяга штанги к поясу в наклоне, спина прямая, лопатки сводятся.",
        ),
        WorkoutExerciseEntity(
            id = "seed-pull-up",
            name = "Подтягивания",
            description = "Подъём тела к перекладине хватом сверху, до подбородка над грифом.",
        ),
        WorkoutExerciseEntity(
            id = "seed-overhead-press",
            name = "Жим стоя",
            description = "Вертикальный жим штанги над головой стоя, корпус зафиксирован.",
        ),
        WorkoutExerciseEntity(
            id = "seed-biceps-curl",
            name = "Сгибания на бицепс",
            description = "Сгибание рук с гантелями, локти прижаты к корпусу.",
        ),
        WorkoutExerciseEntity(
            id = "seed-incline-dumbbell-press",
            name = "Жим гантелей на наклонной",
            description = "Жим гантелей от верха груди на скамье с наклоном 30–45°.",
        ),
        WorkoutExerciseEntity(
            id = "seed-dip",
            name = "Отжимания на брусьях",
            description = "Опускание и подъём тела на брусьях, наклон вперёд — акцент на грудь.",
        ),
        WorkoutExerciseEntity(
            id = "seed-triceps-pushdown",
            name = "Разгибания на блоке",
            description = "Разгибание рук на верхнем блоке вниз, локти неподвижны.",
        ),
    )
}
