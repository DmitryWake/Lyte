package com.nikolaevskii.lyte

import androidx.compose.ui.window.ComposeUIViewController
import com.nikolaevskii.lyte.di.initKoinShared
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        initKoinShared()
        koinStarted = true
    }
    return ComposeUIViewController { App() }
}
