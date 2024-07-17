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
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIViewController
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun kPicker(
    mediaType: MediaType,
    allowMultiple: Boolean,
    maxSelectionCount: Int?,
    maxSizeMb: Int?,
    onMediaPicked: (List<MediaResult>?) -> Unit,
) {
    val viewController = getViewController()
    val effectiveMaxSelectionCount = when {
        allowMultiple -> maxSelectionCount ?: 5 // Default to 5 if null
        else -> maxSelectionCount ?: 1 // Default to 1 if null
    }

    if (allowMultiple) {
        // Use PHPickerViewController for multiple selections
        val configuration = PHPickerConfiguration().apply {
            selectionLimit = effectiveMaxSelectionCount.toLong()
            filter = when (mediaType) {
                MediaType.IMAGE -> PHPickerFilter.imagesFilter()
                MediaType.VIDEO -> PHPickerFilter.videosFilter()
                else -> PHPickerFilter.anyFilterMatchingSubfilters(
                    listOf(PHPickerFilter.imagesFilter(), PHPickerFilter.videosFilter())
                )
            }
        }

        val picker = PHPickerViewController(configuration)
        picker.delegate = PickerDelegate(
            mediaType = mediaType,
            allowMultiple = allowMultiple,
            maxSelectionCount = effectiveMaxSelectionCount,
            maxSizeMb = maxSizeMb,
            onMediaPicked = onMediaPicked
        )

        viewController.presentViewController(picker, true, null)
    } else {
        // Use UIImagePickerController for single selection
        val picker = UIImagePickerController().apply {
            delegate = PickerDelegate(
                mediaType = mediaType,
                allowMultiple = allowMultiple,
                maxSelectionCount = effectiveMaxSelectionCount,
                maxSizeMb = maxSizeMb ?: Int.MAX_VALUE,
                onMediaPicked = onMediaPicked
            )
            mediaTypes = when (mediaType) {
                MediaType.IMAGE -> listOf("public.image")
                MediaType.VIDEO -> listOf("public.movie")
            }
            allowsEditing = false
        }

        viewController.presentViewController(picker, animated = true, completion = null)
    }
}

private fun getViewController(): UIViewController {
    return UIApplication.sharedApplication.keyWindow?.rootViewController ?: UIViewController()
}


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun getFileBytes(path: String): ByteArray = withContext(Dispatchers.IO) {
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
