package com.nikolaevskii.lyte.core.session.domain.model

/**
 * Итог подхода — план против факта. Единая точка правды и для трекинга, и для диффа истории.
 * Соответствие тонам дизайна: [MET] → met, [EXCEEDED] → positive, [MISSED] → negative, [SKIPPED] → skipped.
 */
enum class SessionSetOutcomeEntity {
    MET,
    EXCEEDED,
    MISSED,
    SKIPPED,
}