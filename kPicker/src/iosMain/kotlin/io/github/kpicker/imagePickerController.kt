package io.github.kpicker

import platform.Foundation.NSURL
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject


class ImageVideoPickerDelegate(
    private val mediaType: MediaType,
    private val allowMultiple: Boolean,
    private val maxSelectionCount: Int? = if (allowMultiple) 5 else 1,
    private val maxSizeMb: Int?,
    private val onMediaPicked: (List<MediaResult>?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol,
    PHPickerViewControllerDelegateProtocol {

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
                    MediaType.IMAGE, MediaType.VIDEO -> mediaList.add(
                        MediaResult(
                            uri,
                            name = getFileName(uri),
                            null
                        )
                    )

                    else -> mediaList.add(
                        MediaResult(
                            path = null,
                            name = null,
                            error = "Can't select any type"
                        )
                    )
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

