import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.theolm.rinku.RinkuDesktop

fun main(args: Array<String>) {
    RinkuDesktop(
        applicationId = "dev.theolm.rinku.sample.voyager",
        schemes = setOf("rinku-voyager"),
    ).use { rinku ->
        if (rinku.start(args) == RinkuDesktop.StartResult.FORWARDED) return
        application(exitProcessOnExit = false) {
            Window(onCloseRequest = ::exitApplication, title = "Rinku — Voyager") {
                App()
            }
        }
    }
}
