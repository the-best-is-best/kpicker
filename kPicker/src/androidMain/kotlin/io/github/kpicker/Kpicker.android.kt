package io.github.kpicker

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

actual class Kpicker {
    actual companion object {
        private lateinit var appContext: ComponentActivity

        // Initialize the context
        fun init(context: ComponentActivity) {
            appContext = context
            initializePickers()
        }

        // Retrieve the application context
        fun getAppContext(): Context {
            if (!::appContext.isInitialized) {
                throw IllegalStateException("Kpicker has not been initialized with context.")
            }
            return appContext
        }

        private lateinit var singlePickerLauncher: ActivityResultLauncher<String>
        private lateinit var multiplePickerLauncher: ActivityResultLauncher<String>

        // Initialize the pickers
        private fun initializePickers() {
            val activity = appContext
            singlePickerLauncher =
                activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    handleSingleResult(uri)
                }
            multiplePickerLauncher =
                activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                    handleMultipleResults(uris)
                }
        }

        actual fun pick(
            mediaType: MediaType,
            allowMultiple: Boolean,
            maxSelectionCount: Int?,
            maxSizeMb: Int?,
            onMediaPicked: (List<MediaResult>?) -> Unit
        ) {
            kPicker(mediaType, allowMultiple, maxSelectionCount, maxSizeMb, onMediaPicked)
        }

        private var onMediaPickedCallback: ((List<MediaResult>?) -> Unit)? = null
        private var maxSelectionCount: Int? = null
        private var maxSizeMb: Int? = null

        private fun kPicker(
            mediaType: MediaType,
            allowMultiple: Boolean,
            maxSelectionCount: Int?,
            maxSizeMb: Int?,
            onMediaPicked: (List<MediaResult>?) -> Unit
        ) {

            val mimeType = when (mediaType) {
                MediaType.IMAGE -> "image/*"
                MediaType.VIDEO -> "video/*"
                MediaType.AUDIO -> "audio/*"
                MediaType.FILE -> "*/*"
            }

            Companion.maxSelectionCount = when {
                allowMultiple && (maxSelectionCount == null) -> 5
                !allowMultiple && (maxSelectionCount == null) -> 1
                else -> maxSelectionCount ?: 1
            }

            Companion.maxSizeMb = maxSizeMb
            onMediaPickedCallback = onMediaPicked

            if (allowMultiple) {
                multiplePickerLauncher.launch(mimeType)
            } else {
                singlePickerLauncher.launch(mimeType)
            }
        }

        // Handle the single result
        private fun handleSingleResult(uri: Uri?) {
            val activity = getAppContext() as ComponentActivity
            if (uri != null) {
                val fileName = getFileName(uri, activity.contentResolver)
                val fileSize = getFileSize(uri, activity.contentResolver)
                val file = createFileFromUri(activity, uri, fileName)
                val result = if (maxSizeMb == null || fileSize <= (maxSizeMb!! * 1024 * 1024)) {
                    MediaResult(file.absolutePath, fileName, null)
                } else {
                    MediaResult(null, null, "Selected file exceeds maximum size of $maxSizeMb MB")
                }
                onMediaPickedCallback?.invoke(listOf(result))
            } else {
                onMediaPickedCallback?.invoke(null)
            }
        }

        // Handle multiple results
        private fun handleMultipleResults(uris: List<Uri>) {
            val activity = getAppContext() as ComponentActivity
            val results = uris.mapNotNull { uri ->
                val fileName = getFileName(uri, activity.contentResolver)
                val fileSize = getFileSize(uri, activity.contentResolver)
                val file = createFileFromUri(activity, uri, fileName)
                if (maxSizeMb == null || fileSize <= (maxSizeMb!! * 1024 * 1024)) {
                    MediaResult(file.absolutePath, fileName, null)
                } else {
                    null // File is larger than maxSizeMb, so we ignore it
                }
            }.take(maxSelectionCount!!)
            onMediaPickedCallback?.invoke(results.ifEmpty { null }) // Return null if no valid results
        }


    }
}

// Extension function to read bytes from KFile
actual suspend fun KFile.readBytes(): ByteArray {
    return File(this.path!!).readBytes()
}
