package com.nikolaevskii.lyte.feature.tracker.domain.model

/** Факт по подходу: выполнен с фактическим значением ([Completed]) либо пропущен ([Skipped]). */
sealed interface SessionSetResultEntity {

    data class Completed(val actual: SessionSetValueEntity) : SessionSetResultEntity

    data object Skipped : SessionSetResultEntity
}
