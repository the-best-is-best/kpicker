package io.github.kpicker

import android.content.Context
import android.net.Uri
import java.io.File

internal fun createFileFromUri(context: Context, uri: Uri, fileName: String?): File {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val tempFile = File.createTempFile("temp", null, context.cacheDir)
    tempFile.outputStream().use { input ->
        inputStream.copyTo(input)
    }


    return tempFile
}
