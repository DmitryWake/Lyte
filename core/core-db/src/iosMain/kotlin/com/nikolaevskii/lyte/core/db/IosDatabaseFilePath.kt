package com.nikolaevskii.lyte.core.db

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun iosDatabaseFilePath(name: String): String {
    val documentsUrl: NSURL = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    ) ?: error("Failed to resolve iOS documents directory")
    val documentsPath = requireNotNull(documentsUrl.path) { "Documents URL has no path" }
    return "$documentsPath/$name"
}
