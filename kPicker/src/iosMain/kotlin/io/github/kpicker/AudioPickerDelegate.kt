//package io.github.kpicker
//
//
//import platform.MediaPlayer.MPMediaItem
//import platform.MediaPlayer.MPMediaItemCollection
//import platform.MediaPlayer.MPMediaPickerController
//import platform.MediaPlayer.MPMediaPickerControllerDelegateProtocol
//import platform.MediaPlayer.MPMediaItemPropertyAssetURL
//import platform.MediaPlayer.MPMediaItemPropertyTitle
//import platform.UIKit.UINavigationControllerDelegateProtocol
//import platform.darwin.NSObject
//import kotlin.native.internal.collectReferenceFieldValues
//
//internal class AudioPickerDelegate(
//    private val mediaType: MediaType,
//    private val allowMultiple: Boolean,
//    private val maxSelectionCount: Int? = if (allowMultiple) 5 else 1,
//    private val maxSizeMb: Int?,
//    private val onMediaPicked: (List<MediaResult>?) -> Unit
//) : NSObject(), MPMediaPickerControllerDelegateProtocol {
//    override fun mediaPicker(
//        mediaPicker: MPMediaPickerController,
//        didPickMediaItems: MPMediaItemCollection
//    ) {
//        val mediaList = mutableListOf<MediaResult>()
//        didPickMediaItems.items.forEach { mediaItem ->
//            // Use property accessors instead of collectReferenceFieldValues
//            val uri: String? = mediaItem?.collectReferenceFieldValues(MPMediaItemPropertyAssetURL)?.toString()
//            val fileName: String? = mediaItem?.collectReferenceFieldValues(MPMediaItemPropertyTitle)?.toString()
//            val sizeMb = uri?.let { getFileSizeInMb(it) }
//            if ((sizeMb != null && maxSizeMb != null && sizeMb < maxSizeMb) || sizeMb == null || maxSizeMb == null) {
//                mediaList.add(MediaResult(uri, fileName, null))
//            }
//        }
//        onMediaPicked(mediaList)
//        mediaPicker.dismissViewControllerAnimated(true, completion = null)
//    }
//
//    override fun mediaPickerDidCancel(mediaPicker: MPMediaPickerController) {
//        onMediaPicked(null)
//        mediaPicker.dismissViewControllerAnimated(true, completion = null)
//    }
//}
