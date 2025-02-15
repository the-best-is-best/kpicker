package io.github.kpicker

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

actual fun kPicker(
    mediaType: MediaType,
    allowMultiple: Boolean,
    maxSelectionCount: Int?,
    maxSizeMb: Int?,
    onMediaPicked: (List<MediaResult>?) -> Unit
) {
    val frame = Frame()
    val dialog = FileDialog(frame, "Select Files", FileDialog.LOAD)

    dialog.isMultipleMode = allowMultiple

    // Set filename filter based on media type
    dialog.filenameFilter = FilenameFilter { dir, name ->
        when (mediaType) {
            MediaType.IMAGE -> name.endsWith(".jpg", ignoreCase = true) ||
                    name.endsWith(".png", ignoreCase = true) ||
                    name.endsWith(".jpeg", ignoreCase = true)

            MediaType.VIDEO -> name.endsWith(".mp4", ignoreCase = true) ||
                    name.endsWith(".mov", ignoreCase = true) ||
                    name.endsWith(".avi", ignoreCase = true)

            else -> true // Allow all types if not filtering
        }
    }

    dialog.isVisible = true

    // Get selected files
    val selectedFiles = dialog.files.toList()
    if (selectedFiles.isNotEmpty()) {
        val effectiveMaxSelectionCount = when {
            allowMultiple && maxSelectionCount == null -> 5
            !allowMultiple && maxSelectionCount == null -> 1
            else -> maxSelectionCount ?: 1
        }
        val results = selectedFiles.take(effectiveMaxSelectionCount).mapNotNull { file ->
            val fileSizeMb = file.length() / (1024 * 1024) // Convert bytes to MB
            if (maxSizeMb == null || fileSizeMb <= maxSizeMb) {
                MediaResult(file.absolutePath, null)
            } else {
                null // Exceeds max size
            }
        }

        onMediaPicked(if (results.isEmpty()) null else results)
    } else {
        onMediaPicked(null) // No files selected
    }
}

actual suspend fun getFileBytes(path: String): ByteArray {
    return File(path).readBytes()
}