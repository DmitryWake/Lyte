package com.nikolaevskii.lyte.feature.workout.data

import com.nikolaevskii.lyte.core.db.workout.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.Workout

internal fun WorkoutEntity.toDomain(): Workout = Workout(
    id = id,
    name = name,
    startedAt = startedAt,
)
