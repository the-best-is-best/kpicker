package io.github.kpicker

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileSizeKey
import platform.Foundation.lastPathComponent
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
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

    @OptIn(ExperimentalForeignApi::class)
    private fun getFileSizeInMb(fileUri: String): Double? {
        val fileURL = NSURL(fileURLWithPath = fileUri)
        val fileManager = NSFileManager.defaultManager
        val error: CPointer<ObjCObjectVar<NSError?>>? = null
        val attributes = fileManager.attributesOfItemAtPath(fileURL.path!!, error = error)

        return if (error == null) {
            val fileSizeInBytes = attributes?.get(NSURLFileSizeKey) as? Long ?: 0
            fileSizeInBytes.toDouble() / (1024 * 1024) // Convert bytes to megabytes
        } else {
            null // Handle error (e.g., file not found)
        }
    }
}

fun presentDocumentPicker(maxSizeMb: Int?, onDocumentPicked: (List<MediaResult>?) -> Unit) {
    val documentPicker = UIDocumentPickerViewController(

    )
    documentPicker.delegate = DocumentPickerDelegate(maxSizeMb, onDocumentPicked)
    getViewController().presentViewController(documentPicker, animated = true, completion = null)
}

private fun getViewController(): UIViewController {
    return UIApplication.sharedApplication.keyWindow?.rootViewController ?: UIViewController()
}
