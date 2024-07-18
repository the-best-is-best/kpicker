package io.github.kpicker

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileSizeKey
import platform.Foundation.lastPathComponent
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

internal class DocumentPickerDelegate(
    private val maxSizeMb: Int?,
    private val onDocumentPicked: (List<MediaResult>?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val mediaList = mutableListOf<MediaResult>()
        didPickDocumentsAtURLs.forEach { url ->
            (url as? NSURL)?.let { uri ->
                val uriString = uri.absoluteString
                val sizeMb = uriString?.let { getFileSizeInMb(it) }
                val fileName = uri.lastPathComponent
                if ((sizeMb != null && maxSizeMb != null && sizeMb <= maxSizeMb) || sizeMb == null || maxSizeMb == null) {
                    mediaList.add(MediaResult(uriString, fileName, null))
                }
            }
        }
        onDocumentPicked(mediaList)
        controller.dismissViewControllerAnimated(true, completion = null)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onDocumentPicked(null)
        controller.dismissViewControllerAnimated(true, completion = null)
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun getFileSizeInMb(fileUri: String): Double? {
        val fileURL = NSURL(fileURLWithPath = fileUri)
        val fileManager = NSFileManager.defaultManager
        val error: CPointer<ObjCObjectVar<NSError?>>? = null
        val attributes = fileManager.attributesOfItemAtPath(fileURL.path!!, error = error)

        return if (attributes != null) {
            val fileSizeInBytes = attributes[NSURLFileSizeKey] as? Long ?: 0
            fileSizeInBytes.toDouble() / (1024 * 1024) // Convert bytes to megabytes
        } else {
            error?.pointed?.value?.let {
                println("Error retrieving attributes: $it")
            }
            null // Handle error (e.g., file not found)
        }
    }

}