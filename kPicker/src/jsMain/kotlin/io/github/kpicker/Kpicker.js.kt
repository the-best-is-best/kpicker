package io.github.kpicker

import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.get
import kotlin.math.min

private fun kPicker(
    mediaType: MediaType,
    allowMultiple: Boolean,
    maxSelectionCount: Int?,
    maxSizeMb: Int?,
    onMediaPicked: (List<MediaResult>?) -> Unit
) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = when (mediaType) {
        MediaType.IMAGE -> "image/*"
        MediaType.VIDEO -> "video/*"
        MediaType.AUDIO -> "audio/*"
        MediaType.FILE -> "*/*"
    }
    input.multiple = allowMultiple

    input.onchange = { event: Event ->
        val files = input.files
        val results = mutableListOf<MediaResult>()

        if (files != null) {
            val effectiveMaxSelectionCount = when {
                allowMultiple && maxSelectionCount == null -> 5
                !allowMultiple && maxSelectionCount == null -> 1
                else -> maxSelectionCount ?: 1
            }

            val count = min(effectiveMaxSelectionCount, files.length)
            for (i in 0 until count) {
                val file = files[i]
                if (file != null) {
                    val fileSize = file.size.toDouble() / (1024 * 1024) // Size in MB
                    if (maxSizeMb == null || fileSize <= maxSizeMb) {
                        results.add(MediaResult(file.name, null)) // Use file URL if needed
                    }
                }
            }
        }

        onMediaPicked(results.ifEmpty { null })
    }

    input.click()
}


actual suspend fun KFile.readBytes(): ByteArray {
    getFileBytes(this.path!!)
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
        }
    }
}