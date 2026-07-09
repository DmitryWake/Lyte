package com.nikolaevskii.lyte.feature.splash.domain.repository

/** Персистентный флаг «первый запуск приложения уже прошёл» — не зависит от содержимого доменных таблиц. */
internal interface AppLaunchStateRepository {

    suspend fun hasCompletedFirstLaunch(): Boolean

    suspend fun markFirstLaunchCompleted()
}
