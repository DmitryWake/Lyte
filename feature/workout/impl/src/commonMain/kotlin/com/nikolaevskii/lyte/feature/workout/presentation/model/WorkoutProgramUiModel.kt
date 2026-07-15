package com.nikolaevskii.lyte.feature.workout.presentation.model

import androidx.compose.runtime.Immutable
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity

/** Готовая к отрисовке карточка программы для списка программ. */
@Immutable
data class WorkoutProgramUiModel(
    val id: String,
    val name: String,
    val exerciseCount: Int,
)

internal fun WorkoutItemEntity.toProgramUiModel(): WorkoutProgramUiModel =
    WorkoutProgramUiModel(id = id, name = name, exerciseCount = exerciseCount)
