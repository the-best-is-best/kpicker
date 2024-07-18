package io.github.kpicker

import android.net.Uri
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

// Enum class to represent different media types

actual class Kpicker {
    actual companion object {
        private lateinit var appContext: ComponentActivity

        // Initialize the context
        fun init(context: ComponentActivity) {
            appContext = context
            initializePickers()
        }

        // Retrieve the application context
        fun getAppContext(): ComponentActivity {
            if (!::appContext.isInitialized) {
                throw IllegalStateException("Kpicker has not been initialized with context.")
            }
            return appContext
        }

        private lateinit var singlePickerLauncherImage: ActivityResultLauncher<String>
        private lateinit var multiplePickerLauncherImage: ActivityResultLauncher<String>
        private lateinit var singlePickerLauncherVideo: ActivityResultLauncher<String>
        private lateinit var multiplePickerLauncherVideo: ActivityResultLauncher<String>
        private lateinit var singlePickerLauncherAudio: ActivityResultLauncher<String>
        private lateinit var multiplePickerLauncherAudio: ActivityResultLauncher<String>
        private lateinit var singlePickerLauncherFile: ActivityResultLauncher<Array<String>>
        private lateinit var multiplePickerLauncherFile: ActivityResultLauncher<Array<String>>

        // Initialize the pickers
        private fun initializePickers() {
            val activity = appContext
            singlePickerLauncherImage =
                activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    handleSingleResult(MediaType.IMAGE, uri)
                }
            multiplePickerLauncherImage =
                activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                    handleMultipleResults(MediaType.IMAGE, uris)
                }
            singlePickerLauncherVideo =
                activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    handleSingleResult(MediaType.VIDEO, uri)
                }
            multiplePickerLauncherVideo =
                activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                    handleMultipleResults(MediaType.VIDEO, uris)
                }
            singlePickerLauncherAudio =
                activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    handleSingleResult(MediaType.AUDIO, uri)
                }
            multiplePickerLauncherAudio =
                activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                    handleMultipleResults(MediaType.AUDIO, uris)
                }
            singlePickerLauncherFile =
                activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    handleSingleResult(MediaType.FILE, uri)
                }
            multiplePickerLauncherFile =
                activity.registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                    handleMultipleResults(MediaType.FILE, uris)
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
            Companion.maxSelectionCount = when {
                allowMultiple && (maxSelectionCount == null) -> 5
                !allowMultiple && (maxSelectionCount == null) -> 1
                else -> maxSelectionCount ?: 1
            }

            Companion.maxSizeMb = maxSizeMb
            onMediaPickedCallback = onMediaPicked

            when (mediaType) {
                MediaType.IMAGE -> {
                    if (allowMultiple) {
                        multiplePickerLauncherImage.launch("image/*")
                    } else {
                        singlePickerLauncherImage.launch("image/*")
                    }
                }

                MediaType.VIDEO -> {
                    if (allowMultiple) {
                        multiplePickerLauncherVideo.launch("video/*")
                    } else {
                        singlePickerLauncherVideo.launch("video/*")
                    }
                }

                MediaType.AUDIO -> {
                    if (allowMultiple) {
                        multiplePickerLauncherAudio.launch("audio/*")
                    } else {
                        singlePickerLauncherAudio.launch("audio/*")
                    }
                }

                MediaType.FILE -> {
                    if (allowMultiple) {
                        multiplePickerLauncherFile.launch(arrayOf("*/*"))
                    } else {
                        singlePickerLauncherFile.launch(arrayOf("*/*"))
                    }
                }
            }
        }

        // Handle the single result
        private fun handleSingleResult(mediaType: MediaType, uri: Uri?) {
            val activity = getAppContext()
            if (uri != null) {
                val fileName = getFileName(uri, activity.contentResolver)
                val fileSize = getFileSize(uri, activity.contentResolver)
                val file = createFileFromUri(activity, uri, fileName)
                val result = if (maxSizeMb == null || fileSize <= (maxSizeMb!! * 1024 * 1024)) {
                    MediaResult(file.absolutePath, fileName, null)
                } else {
                    MediaResult(null, null, "Selected file exceeds maximum size of $maxSizeMb MB")
                }
                onMediaPickedCallback?.invoke(listOfNotNull(result))
            } else {
                onMediaPickedCallback?.invoke(null)
            }
        }

        // Handle multiple results
        private fun handleMultipleResults(mediaType: MediaType, uris: List<Uri>) {
            val activity = getAppContext()
            val results = uris.take(maxSelectionCount ?: 1).mapNotNull { uri ->
                val fileName = getFileName(uri, activity.contentResolver)
                val fileSize = getFileSize(uri, activity.contentResolver)
                val file = createFileFromUri(activity, uri, fileName)
                if (maxSizeMb == null || fileSize <= (maxSizeMb!! * 1024 * 1024)) {
                    MediaResult(file.absolutePath, fileName, null)
                } else {
                    null // File is larger than maxSizeMb, so we ignore it
                }
            }

            onMediaPickedCallback?.invoke(results.ifEmpty { null }) // Return null if no valid results
        }


    }
}
// Extension function to read bytes from KFile
actual suspend fun KFile.readBytes(): ByteArray {
    return File(this.path!!).readBytes()
}

actual suspend fun KFile.getBase64(): String {
    val bytes = this.readBytes()

    return Base64.encodeToString(bytes, Base64.DEFAULT)
}