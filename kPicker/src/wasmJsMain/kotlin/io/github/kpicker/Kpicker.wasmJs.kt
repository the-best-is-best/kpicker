package io.github.kpicker

// In your wasmMain source set
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.Response
import org.w3c.files.File
import org.w3c.files.get
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

    input.onchange = {
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
                val file = files[i] as File
                val fileSize = file.size.toDouble() / (1024 * 1024) // Size in MB

                if (maxSizeMb == null || fileSize <= maxSizeMb) {
                    results.add(
                        MediaResult(
                            file.name,
                            null
                        )
                    ) // Store filename or more metadata if needed
                }
            }
        }

        onMediaPicked(results.ifEmpty { null })
    }

    // Trigger the file input click
    input.click()
}

actual suspend fun getFileBytes(path: String): ByteArray {
    val response = window.fetch(path).await<Response>()
    val arrayBuffer = response.arrayBuffer().await<ArrayBuffer>()

// Convert ArrayBuffer to ByteArray
    val byteArray = Int8Array(arrayBuffer).let { typedArray ->
        ByteArray(typedArray.length) { index -> typedArray[index] }
    }

    return byteArray

}