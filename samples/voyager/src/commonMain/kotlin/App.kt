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
    var deepLink by remember { mutableStateOf<DeepLink?>(null) }

    DeepLinkListener {
        deepLink = it
    }

    MainScreen(deepLink)
}

@Composable
private fun MainScreen(deepLink: DeepLink?) {
    val screenStack = buildScreenStack(deepLink)

    MaterialTheme {
        Navigator(
            screens = screenStack,
            key = deepLink?.hashCode()?.toString() ?: "null"
        )
    }
}


private fun buildScreenStack(deepLink: DeepLink?): List<Screen> {
    if (deepLink == null) {
        return listOf(FirstScreen(deepLink))
    }

    val screenPaths = deepLink.pathSegments.mapNotNull {
        when (it) {
            "second" -> SecondScreen()
            "third" -> ThirdScreen()
            "fourth" -> FourthScreen()
            else -> null
        }
    }

    return listOf(FirstScreen(deepLink)) + screenPaths
}
