package io.github.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kpermissions.handler.PermissionHandler
import io.github.kpicker.Kpicker

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PermissionHandler.init(this)
        Kpicker.init(this)
        setContent { App() }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
