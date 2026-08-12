package com.nikolaevskii.lyte.core.session.domain.model

import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph

/**
 * Снапшот программы на момент старта сессии. [id] — ссылка на программу (архивируется, а не удаляется,
 * пока на неё ссылаются сессии); [name], [accent] и [glyph] — копии на случай последующего
 * переименования, перекраски или удаления программы: карточка в истории обязана пережить и то, и другое.
 */
data class SessionProgramEntity(
    val id: String,
    val name: String,
    val accent: ExerciseAccent = ExerciseAccent.Default,
    val glyph: ExerciseGlyph = ExerciseGlyph.Default,
)
