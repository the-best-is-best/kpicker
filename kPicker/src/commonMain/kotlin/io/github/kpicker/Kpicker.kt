package io.github.kpicker


expect suspend fun KFile.readBytes(): ByteArray

val KFile.baseName: String
    get() = name!!.substringBeforeLast(".", name)

val KFile.extension: String
    get() = name!!.substringAfterLast(".")


expect class Kpicker {
    companion object {
        fun pick(
            mediaType: MediaType,
            allowMultiple: Boolean = false,
            maxSelectionCount: Int? = if (allowMultiple) 5 else 1,
            maxSizeMb: Int? = null,
            onMediaPicked: (List<MediaResult>?) -> Unit
        )
    }
}

expect suspend fun KFile.getBase64(): String