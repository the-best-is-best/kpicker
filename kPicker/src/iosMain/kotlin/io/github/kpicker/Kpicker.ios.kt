package io.github.kpicker

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreServices.kUTTypeImage
import platform.CoreServices.kUTTypeItem
import platform.CoreServices.kUTTypeMovie
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIViewController

actual class Kpicker {
    actual companion object {
        actual fun pick(
            mediaType: MediaType,
            allowMultiple: Boolean,
            maxSelectionCount: Int?,
            maxSizeMb: Int?,
            onMediaPicked: (List<MediaResult>?) -> Unit
        ) {
            kPicker(
                mediaType, allowMultiple, maxSelectionCount, maxSizeMb, onMediaPicked
            )
        }
    }
}


@OptIn(ExperimentalForeignApi::class)
private fun kPicker(
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

    when (mediaType) {
        MediaType.IMAGE, MediaType.VIDEO -> {
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
                picker.delegate = ImageVideoPickerDelegate(
                    mediaType = mediaType,
                    allowMultiple = allowMultiple,
                    maxSelectionCount = effectiveMaxSelectionCount,
                    maxSizeMb = maxSizeMb,
                    onMediaPicked = onMediaPicked
                )
                viewController.presentViewController(picker, animated = true, completion = null)
            } else {
                // Use UIImagePickerController for single selection
                val picker = UIImagePickerController().apply {
                    delegate = ImageVideoPickerDelegate(
                        mediaType = mediaType,
                        allowMultiple = allowMultiple,
                        maxSelectionCount = effectiveMaxSelectionCount,
                        maxSizeMb = maxSizeMb,
                        onMediaPicked = onMediaPicked
                    )
                    mediaTypes = when (mediaType) {
                        MediaType.IMAGE -> listOf(kUTTypeImage as String)
                        MediaType.VIDEO -> listOf(kUTTypeMovie as String)
                        else -> listOf(kUTTypeImage as String, kUTTypeMovie as String)
                    }
                }
                viewController.presentViewController(picker, animated = true, completion = null)
            }
        }

//        MediaType.AUDIO -> {
//            val picker = MPMediaPickerController().apply {
//                this.delegate = AudioPickerDelegate(
//                    mediaType = mediaType,
//                    allowMultiple = allowMultiple,
//                    maxSelectionCount = effectiveMaxSelectionCount,
//                    maxSizeMb = maxSizeMb,
//                    onMediaPicked = onMediaPicked
//                )
//                this.allowsPickingMultipleItems = allowMultiple
//            }
//            viewController.presentViewController(picker, animated = true, completion = null)
//        }

        MediaType.FILE -> {
            val documentPicker = UIDocumentPickerViewController(
                documentTypes = listOf(kUTTypeItem as String),
                inMode = if (allowMultiple) UIDocumentPickerMode.UIDocumentPickerModeImport else UIDocumentPickerMode.UIDocumentPickerModeOpen
            ).apply {
                this.delegate = DocumentPickerDelegate(
                    maxSizeMb = maxSizeMb,
                    onDocumentPicked = onMediaPicked
                )
            }
            viewController.presentViewController(documentPicker, animated = true, completion = null)
        }
    }
}

private fun getViewController(): UIViewController {
    return UIApplication.sharedApplication.keyWindow?.rootViewController ?: UIViewController()
}


actual suspend fun KFile.readBytes(): ByteArray {
    return getFileBytes(this.path!!)
}

