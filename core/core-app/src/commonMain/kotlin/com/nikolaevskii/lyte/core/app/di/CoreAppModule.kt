package com.nikolaevskii.lyte.core.app.di

import com.nikolaevskii.lyte.core.app.data.repository.AppLaunchStateRepositoryImpl
import com.nikolaevskii.lyte.core.app.domain.repository.AppLaunchStateRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/** DAO приходит из `coreDbModule()`. */
fun coreAppModule(): Module = module {
    single<AppLaunchStateRepository> { AppLaunchStateRepositoryImpl(appLaunchStateDao = get()) }
}
