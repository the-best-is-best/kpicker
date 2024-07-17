package io.github.kpicker

expect fun kPicker(
    mediaType: MediaType,
    allowMultiple: Boolean = false,
    maxSelectionCount: Int? = if (allowMultiple) 5 else 1,
    maxSizeMb: Int? = null,
    onMediaPicked: (List<MediaResult>?) -> Unit
)

data class MediaResult(val uri: String?, val error: String?)

enum class MediaType {
    IMAGE, VIDEO
}

expect suspend fun getFileBytes(path: String): ByteArray