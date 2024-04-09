import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.compose.ext.DeepLinkListener
import screens.First

/**
 * Represents the root composable function of the application. It listens for deep link changes
 * and updates the UI accordingly by navigating to the relevant screen.
 * Depending on the deep link received, the application navigates to different screens within the app.
 */
@Composable
fun App() {
    // State holding the current deep link. Initially, there is no deep link (null).
    var deepLink by remember { mutableStateOf<DeepLink?>(null) }

    // Listens for deep link changes. When a deep link is received, the state is updated.
    DeepLinkListener {
        deepLink = it
    }

    // Renders the main screen of the application, passing the current deep link.
    MainScreen(deepLink)
}

/**
 * The main screen of the application. It decides which screen stack to display based on the
 * current deep link.
 *
 * @param deepLink The current deep link received by the application. It may be null, indicating
 * that no deep link is currently being handled.
 */
@Composable
private fun MainScreen(deepLink: DeepLink?) {
    // Calculates the screen stack to be displayed based on the deep link. This is recalculated
    // every time the deep link changes.
    val screenStack = remember(deepLink) {
        deepLink.toScreenStack()
    }

    MaterialTheme {
        // Initializes the navigator with the first screen. Depending on the current screen stack,
        // it may navigate to different screens within the app.
        Navigator(screen = First()) {
            CurrentScreen()
            // Replaces the entire screen stack based on the current deep link. This ensures that
            // the app navigates to the appropriate screen.
            remember(screenStack) {
                it.replaceAll(screenStack)
            }
        }
    }
}
