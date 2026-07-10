package com.nikolaevskii.lyte.feature.tracker.domain.model

/**
 * Снапшот программы на момент старта сессии. [id] — ссылка на программу (архивируется, а не удаляется,
 * пока на неё ссылаются сессии); [name] — копия имени на случай последующего переименования программы.
 */
data class SessionProgramEntity(
    val id: String,
    val name: String,
)
