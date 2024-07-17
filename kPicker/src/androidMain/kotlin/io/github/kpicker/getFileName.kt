package io.github.kpicker

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

// Get the file name from the URI
internal fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
    var fileName: String? = null
    val returnCursor = contentResolver.query(uri, null, null, null, null)
    returnCursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    return fileName
}

