package io.github.kpicker

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDataReadingUncached
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal suspend fun getFileBytes(path: String): ByteArray = withContext(Dispatchers.IO) {
    memScoped {
        val nsUrl = NSURL.URLWithString(path)
        // Start accessing the security scoped resource
        nsUrl!!.startAccessingSecurityScopedResource()

        // Read the data
        val error: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
        val nsData = NSData.dataWithContentsOfURL(nsUrl, NSDataReadingUncached, error)
            ?: throw IllegalStateException("Failed to read data from $nsUrl. Error: ${error.pointed.value}")

        // Stop accessing the security scoped resource
        nsUrl.stopAccessingSecurityScopedResource()

        // Copy the data to a ByteArray
        ByteArray(nsData.length.toInt()).apply {
            memcpy(this.refTo(0), nsData.bytes, nsData.length)
        }
    }

}