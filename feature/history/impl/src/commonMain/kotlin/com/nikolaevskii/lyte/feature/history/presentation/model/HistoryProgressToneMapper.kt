package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetOutcomeEntity

/**
 * Исход подхода в тон трека. Общий для списка Истории (5.1, трек карточки сессии) и деталей сессии
 * (5.2, строка диффа): один и тот же исход обязан выглядеть на обоих экранах одинаково.
 */
internal fun SessionSetOutcomeEntity?.toProgressTone(): LyteProgressTone = when (this) {
    SessionSetOutcomeEntity.MET -> LyteProgressTone.Met
    SessionSetOutcomeEntity.EXCEEDED -> LyteProgressTone.Positive
    SessionSetOutcomeEntity.MISSED -> LyteProgressTone.Negative
    // SKIPPED и невыполненный (null, в завершённой сессии не встречается) — «пропущено».
    SessionSetOutcomeEntity.SKIPPED, null -> LyteProgressTone.Skipped
}
