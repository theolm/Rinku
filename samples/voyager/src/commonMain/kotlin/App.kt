import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.DeepLinkListener
import screens.FourthScreen
import screens.SecondScreen
import screens.ThirdScreen
import screens.FirstScreen

@Composable
fun App() {
    var screenStack by remember { mutableStateOf(ScreenStack()) }

    LaunchedEffect(screenStack) {
        println("screenStack ${screenStack.screens}")
    }

    MaterialTheme {
        Navigator(
            screens = screenStack.screens,
        )
    }

    DeepLinkListener {
        screenStack = buildScreenStack(it)
    }
}



private fun buildScreenStack(deepLink: DeepLink): ScreenStack {
    val screenPaths = deepLink.pathSegments.mapNotNull {
        when (it) {
            "second" -> SecondScreen()
            "third" -> ThirdScreen()
            "fourth" -> FourthScreen()
            else -> null
        }
    }

    return ScreenStack(listOf(FirstScreen()) + screenPaths)
}

class ScreenStack(val screens: List<Screen> = listOf(FirstScreen()))
