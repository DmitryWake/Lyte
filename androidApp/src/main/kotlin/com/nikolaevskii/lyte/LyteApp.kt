package com.nikolaevskii.lyte

import android.app.Application
import com.nikolaevskii.lyte.di.initKoinShared
import org.koin.android.ext.koin.androidContext

class LyteApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoinShared {
            androidContext(this@LyteApp)
        }
    }
}
