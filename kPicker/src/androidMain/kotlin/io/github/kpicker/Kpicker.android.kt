package io.github.kpicker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

class Kpicker {
    companion object {
        private lateinit var appContext: Context

        fun getAppContext(): Context {

            if (!::appContext.isInitialized) {
                throw IllegalStateException("PermissionHandler has not been initialized with context.")
            }
            return appContext
        }

        fun init(context: Context) {
            appContext = context
        }
    }

}

actual fun kPicker(
    mediaType: MediaType,
    allowMultiple: Boolean,
    maxSelectionCount: Int?,
    maxSizeMb: Int?,
    onMediaPicked: (List<MediaResult>?) -> Unit
) {

    val activity = Kpicker.getAppContext() as ComponentActivity

    val mimeType = when (mediaType) {
        MediaType.IMAGE -> "image/*"
        MediaType.VIDEO -> "video/*"
    }
    val effectiveMaxSelectionCount = when {
        allowMultiple && (maxSelectionCount == null) -> 5
        !allowMultiple && (maxSelectionCount == null) -> 1
        else -> maxSelectionCount ?: 1
    }
    val pickerLauncher = if (allowMultiple) {
        activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            val results = uris.mapNotNull { uri ->
                val fileSize = getFileSize(uri, activity.contentResolver)
                if (maxSizeMb == null || fileSize <= (maxSizeMb * 1024 * 1024)) {
                    MediaResult(uri.toString(), null)
                } else {
                    null // File is larger than maxSizeMb, so we ignore it
                }
            }.take(effectiveMaxSelectionCount)
            onMediaPicked(results.ifEmpty { null }) // Return null if no valid results
        }
    } else {
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val fileSize = getFileSize(uri, activity.contentResolver)
                val result = if (maxSizeMb == null || fileSize <= (maxSizeMb * 1024 * 1024)) {
                    MediaResult(uri.toString(), null)
                } else {
                    MediaResult(null, "Selected file exceeds maximum size of $maxSizeMb MB")
                }
                onMediaPicked(listOf(result))
            } else {
                onMediaPicked(null)
            }
        }
    }

    pickerLauncher.launch(mimeType)
}

private fun getFileSize(uri: Uri, contentResolver: ContentResolver): Long {
    val returnCursor = contentResolver.query(uri, null, null, null, null)
    val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE) ?: -1
    returnCursor?.moveToFirst()
    val size = if (sizeIndex != -1) returnCursor!!.getLong(sizeIndex) else 0L
    returnCursor?.close()
    return size
}

actual suspend fun getFileBytes(path: String): ByteArray {
    return File(path).readBytes()
}