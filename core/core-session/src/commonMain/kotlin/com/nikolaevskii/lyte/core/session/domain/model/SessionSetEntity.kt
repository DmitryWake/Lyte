package com.nikolaevskii.lyte.core.session.domain.model

/**
 * Подход внутри упражнения сессии: план ([target]) и факт ([result], `null` — ещё не выполнялся).
 */
data class SessionSetEntity(
    val id: String,
    val target: SessionSetValueEntity,
    val result: SessionSetResultEntity?,
    val note: String,
)
