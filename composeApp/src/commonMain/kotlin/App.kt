import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import home.HomeScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(HomeScreen())
    }
}
