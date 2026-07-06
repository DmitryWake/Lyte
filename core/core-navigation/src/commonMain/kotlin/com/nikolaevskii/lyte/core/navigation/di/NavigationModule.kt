package com.nikolaevskii.lyte.core.navigation.di

import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.LyteNavigatorImpl
import org.koin.dsl.module

val coreNavigationModule = module {
    single<LyteNavigator> { LyteNavigatorImpl() }
}