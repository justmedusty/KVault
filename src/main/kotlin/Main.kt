import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import composables.app


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        app()
    }
}
