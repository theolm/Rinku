import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModule())
    }

    Window(onCloseRequest = ::exitApplication, title = "Temp") {
        App()
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}