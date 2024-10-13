import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.common.screens.FirstScreen
import dev.theolm.rinku.common.screens.FourthScreen
import dev.theolm.rinku.common.screens.SecondScreen
import dev.theolm.rinku.common.screens.ThirdScreen
import dev.theolm.rinku.compose.ext.DeepLinkListener

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
    val component = remember(deepLink) {
        AppComponentImpl(
            initialStack = deepLink.toScreenStack(),
            componentContext = DefaultComponentContext(LifecycleRegistry())
        )
    }

    MaterialTheme {
        Children(
            stack = component.stack,
            modifier = Modifier.fillMaxSize(),
            animation = stackAnimation(slide()),
        ) {
            when (val screen = it.instance) {
                is AppComponent.Screen.First -> FirstScreen(deepLink)
                is AppComponent.Screen.Second -> SecondScreen(
                    onBackPress = {
                        component.onBackPress()
                    },
                    randomArgument = screen.randomArguments
                )
                is AppComponent.Screen.Third -> ThirdScreen(
                    onBackPress = {
                        component.onBackPress()
                    },
                    randomArgument = screen.randomArguments
                )

                is AppComponent.Screen.Fourth -> FourthScreen(
                    onBackPress = {
                        component.onBackPress()
                    },
                    randomArgument = screen.randomArguments
                )
            }
        }
    }
}
