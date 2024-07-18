package io.github.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kpermissions.handler.PermissionHandler
import io.github.kpicker.KFile
import io.github.kpicker.Kpicker
import io.github.kpicker.MediaType
import io.github.kpicker.readBytes
import io.github.sample.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
internal fun App() = AppTheme {
    val scope = rememberCoroutineScope()
    var file: KFile? = null
    val permission = PermissionHandler()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                var androidPer1 = false
                var androidPer2 = false
                var iosPer = false

                Kpicker.pick(
                    allowMultiple = true,
                    mediaType = MediaType.FILE,
                    onMediaPicked = {

                        file = KFile(
                            path = it?.first()?.path,
                            name = it?.first()?.name
                        )
                        println(" file name is ${file?.name} file path is ${file!!.path}")

                    })

            }) {
                Text("Single video picker")
            }

            Spacer(Modifier.height(10.dp))

            Button(onClick = {
                if (file != null && file!!.path != null) {

                    scope.launch {
                        val bytes = file!!.readBytes()
                        println("bytes ${bytes.size}")
                    }
                }

            }) {
                Text("get bytes")
            }
        }
    }

}
