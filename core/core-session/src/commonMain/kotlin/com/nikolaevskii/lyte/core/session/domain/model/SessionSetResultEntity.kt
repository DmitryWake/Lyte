package com.nikolaevskii.lyte.core.session.domain.model

/** Факт по подходу: выполнен с фактическим значением ([Completed]) либо пропущен ([Skipped]). */
sealed interface SessionSetResultEntity {

    data class Completed(val actual: SessionSetValueEntity) : SessionSetResultEntity

    data object Skipped : SessionSetResultEntity
}
