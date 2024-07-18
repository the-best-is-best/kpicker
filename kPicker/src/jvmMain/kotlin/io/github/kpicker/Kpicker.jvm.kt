package io.github.kpicker

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import java.util.Base64


private fun kPicker(
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
                MediaResult(file.absolutePath, file.name, null)
            } else {
                null // Exceeds max size
            }
        }

        onMediaPicked(if (results.isEmpty()) null else results)
    } else {
        onMediaPicked(null) // No files selected
    }
}

private suspend fun getFileBytes(path: String): ByteArray {
    return File(path).readBytes()
}


actual suspend fun KFile.readBytes(): ByteArray {
    return getFileBytes(this.path!!)
}

actual class Kpicker {
    actual companion object {
        actual fun pick(
            mediaType: MediaType,
            allowMultiple: Boolean,
            maxSelectionCount: Int?,
            maxSizeMb: Int?,
            onMediaPicked: (List<MediaResult>?) -> Unit
        ) {
            kPicker(
                mediaType, allowMultiple, maxSelectionCount, maxSizeMb, onMediaPicked
            )
        }
    }
}

actual suspend fun KFile.getBase64(): String {
    return Base64.getEncoder().encodeToString(this.readBytes())
}