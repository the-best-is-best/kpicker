package io.github.kpicker

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

// Get the file size from the URI
internal fun getFileSize(uri: Uri, contentResolver: ContentResolver): Long {
    val returnCursor = contentResolver.query(uri, null, null, null, null)
    val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE) ?: -1
    returnCursor?.moveToFirst()
    val size = if (sizeIndex != -1) returnCursor!!.getLong(sizeIndex) else 0L
    returnCursor?.close()
    return size
}
