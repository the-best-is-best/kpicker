package io.github.kpicker

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileSizeKey


@OptIn(ExperimentalForeignApi::class)
internal fun getFileSizeInMb(fileUri: String): Double? {
    val fileURL = NSURL(fileURLWithPath = fileUri)

    // Use NSFileManager to get file attributes
    val fileManager = NSFileManager.defaultManager
    val error: CPointer<ObjCObjectVar<NSError?>>? = null
    val attributes = fileManager.attributesOfItemAtPath(fileURL.path!!, error = error)

    return if (error == null) {
        // Get the file size in bytes
        val fileSizeInBytes = attributes?.get(NSURLFileSizeKey) as? Long ?: 0
        // Convert bytes to megabytes (MB)
        fileSizeInBytes.toDouble() / (1024 * 1024)
    } else {
        // Handle error (e.g., file not found)
        null
    }
}