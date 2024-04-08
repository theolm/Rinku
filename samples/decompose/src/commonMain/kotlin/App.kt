import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import components.toScreenStack
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.compose.ext.DeepLinkListener
import screens.FourthScreen
import screens.SecondScreen
import screens.ThirdScreen
import screens.FirstScreen


/**
 * Main entry point of the app.
 * It contains the main screen of the app.
 * It also listens for deep links and updates the deep link state.
 * The deep link state is passed to the main screen.
 * The main screen builds a screen stack based on the deep link.
 */
@Composable
fun App() {
    var deepLink by remember { mutableStateOf<DeepLink?>(null) }

    DeepLinkListener {
        deepLink = it
    }

    MainScreen(deepLink)
}

/**
 * Main screen of the app.
 * It contains a stack of screens.
 */
@Composable
private fun MainScreen(deepLink: DeepLink?) {
    val screenStack = deepLink.toScreenStack()
    val component = remember(screenStack) {
        AppComponentImpl(
            initialStack = screenStack,
            componentContext = DefaultComponentContext(LifecycleRegistry())
        )
    }

    MaterialTheme {
        Children(
            stack = component.stack,
            modifier = Modifier.fillMaxSize(),
            animation = stackAnimation(fade()),
        ) {
            when (it.instance) {
                is AppComponent.Screen.First -> FirstScreen(deepLink)
                is AppComponent.Screen.Second -> SecondScreen {
                    component.onBackPress()
                }

                is AppComponent.Screen.Third -> ThirdScreen {
                    component.onBackPress()
                }

                is AppComponent.Screen.Fourth -> FourthScreen {
                    component.onBackPress()
                }
            }
        }
    }
}
