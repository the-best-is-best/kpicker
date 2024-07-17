package io.github.kpicker

import platform.Foundation.NSURL
import platform.Foundation.lastPathComponent

internal fun getFileName(fileUri: String): String? {
    val fileURL = NSURL(fileURLWithPath = fileUri)
    return fileURL.lastPathComponent
}