package io.github.kpicker

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileSizeKey
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject


class PickerDelegate(
    private val mediaType: MediaType,
    private val allowMultiple: Boolean,
    private val maxSelectionCount: Int? = if (allowMultiple) 5 else 1,
    private val maxSizeMb: Int?,
    private val onMediaPicked: (List<MediaResult>?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol,
    PHPickerViewControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {

        val mediaList = mutableListOf<MediaResult>()
        didFinishPickingMediaWithInfo.let {
            println("media type is $mediaType")
            val uri: String? = when (mediaType) {
                MediaType.IMAGE -> (it[UIImagePickerControllerImageURL] as? NSURL)?.absoluteString
                MediaType.VIDEO -> (it[UIImagePickerControllerMediaURL] as? NSURL)?.absoluteString
                else -> null
            }
            println("uri $uri")
            val sizeMb = getFileSizeInMb(uri!!)
            if ((sizeMb != null && maxSizeMb != null && sizeMb < maxSizeMb) || sizeMb == null || maxSizeMb == null) {
                when (mediaType) {
                    MediaType.IMAGE, MediaType.VIDEO -> mediaList.add(MediaResult(uri, null))
                    else -> mediaList.add(MediaResult(uri = null, error = "Can't select any type"))
                }
            }
            if (mediaList.size > maxSelectionCount!!) {
                mediaList.subList(0, maxSelectionCount)
            }
            onMediaPicked(mediaList)
            picker.dismissViewControllerAnimated(true, completion = null)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onMediaPicked(null)
        picker.dismissViewControllerAnimated(true, completion = null)
    }

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {

    }
}

@OptIn(ExperimentalForeignApi::class)
private
fun getFileSizeInMb(fileUri: String): Double? {
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
