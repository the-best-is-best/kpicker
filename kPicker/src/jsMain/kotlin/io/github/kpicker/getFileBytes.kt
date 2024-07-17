package io.github.kpicker

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.Promise


internal suspend fun getFileBytes(path: String): ByteArray {
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