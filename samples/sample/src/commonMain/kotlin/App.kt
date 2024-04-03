import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import dev.theolm.rinku.LocalDeepLink
import home.HomeScreen

@Composable
fun App() {

    val deepLink = LocalDeepLink.current
    println(deepLink)

    MaterialTheme {
        Navigator(HomeScreen())
    }
}
