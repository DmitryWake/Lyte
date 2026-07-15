package com.nikolaevskii.lyte.feature.tracker.presentation.model

import androidx.compose.runtime.Immutable
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity

/** Готовая к отрисовке карточка программы для списков трекера (выбор программы). */
@Immutable
data class WorkoutProgramUiModel(
    val id: String,
    val name: String,
    val exerciseCount: Int,
)

internal fun WorkoutItemEntity.toProgramUiModel(): WorkoutProgramUiModel =
    WorkoutProgramUiModel(id = id, name = name, exerciseCount = exerciseCount)
