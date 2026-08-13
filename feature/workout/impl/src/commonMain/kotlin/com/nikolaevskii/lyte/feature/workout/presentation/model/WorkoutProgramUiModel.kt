package com.nikolaevskii.lyte.feature.workout.presentation.model

import androidx.compose.runtime.Immutable
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity

/** Готовая к отрисовке карточка программы для списка программ. */
@Immutable
data class WorkoutProgramUiModel(
    val id: String,
    val name: String,
    val exerciseCount: Int,
    val accent: LyteAccent = LyteAccent.Default,
    val glyph: LyteExerciseGlyph = LyteExerciseGlyph.Default,
)

internal fun WorkoutItemEntity.toProgramUiModel(): WorkoutProgramUiModel =
    WorkoutProgramUiModel(
        id = id,
        name = name,
        exerciseCount = exerciseCount,
        accent = accent.toLyteAccent(),
        glyph = glyph.toLyteGlyph(),
    )
