package screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import components.DemoScaffold

class SecondScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DemoScaffold(
            title = "Second",
            onBackPress = {
                navigator.pop()
            }
        ) {
            Text("Second Screen")
        }
    }
}
