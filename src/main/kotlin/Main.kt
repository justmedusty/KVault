import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import composables.app


fun main() = application {
    Window(onCloseRequest = ::exitApplication,
        title = "KVault", focusable = true, icon = BitmapPainter(useResource("icon.png", ::loadImageBitmap)),
    ) {
        app()
    }
}
