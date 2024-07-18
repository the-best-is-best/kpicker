package io.github.kpicker


import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.PhotosUI.PHPickerResult
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


    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        val mediaList = mutableListOf<MediaResult>()
        var selectedCount = 0

        didFinishPicking.forEach { item ->
            if (item is PHPickerResult) {
                item.itemProvider.loadItemForTypeIdentifier(
                    when (mediaType) {
                        MediaType.IMAGE -> "public.image"
                        MediaType.VIDEO -> "public.movie"
                        else -> "error know the type"
                    },
                    options = null,
                    completionHandler = { data, error ->
                        if (data != null) {
                            val mediaUri = (data as? NSURL)?.absoluteString
                            val mediaResult = MediaResult(
                                mediaUri,
                                name = mediaUri?.let { getFileName(it) },
                                error = null
                            )
                            mediaList.add(mediaResult)
                        } else {
                            // Handle error or no data scenario
                            println("Error loading media: $error")
                        }

                        selectedCount++
                        if (selectedCount >= didFinishPicking.size || selectedCount >= maxSelectionCount!!) {
                            // Perform UIKit operations on the main thread
                            NSOperationQueue.mainQueue.addOperationWithBlock {
                                picker.dismissViewControllerAnimated(true, completion = null)
                                onMediaPicked(mediaList)
                            }
                        }
                    }
                )
            }
        }
    }

}