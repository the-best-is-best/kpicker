package io.github.kpicker

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.Promise
import kotlin.math.min

actual fun kPicker(
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

actual suspend fun getFileBytes(path: String): ByteArray {
    return Promise { resolve, reject ->
        val inputElement = document.createElement("input") as HTMLInputElement
        inputElement.type = "file"

        inputElement.onchange = { event ->
            val file = inputElement.files?.get(0)
            if (file != null) {
                val reader = FileReader()

                reader.onload = {
                    val arrayBuffer = reader.result as? ArrayBuffer
                    val bytes = arrayBuffer?.let { arr ->
                        ByteArray(arr.byteLength) { index ->
                            (arr as Uint8Array)[index]
                        }
                    }
                    resolve(bytes)
                }

                reader.onerror = {
                    reject(Error("Error reading file"))
                }

                reader.readAsArrayBuffer(file)
            } else {
                reject(Error("No file selected"))
            }
        }

        // Programmatically trigger the file input dialog
        inputElement.click()
    }.await()!!
}