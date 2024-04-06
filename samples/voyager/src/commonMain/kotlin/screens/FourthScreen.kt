package screens

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import components.DemoScaffold
import dev.theolm.rinku.Rinku

class FourthScreen() : Screen  {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DemoScaffold(
            title = "Fourth",
            onBackPress = {
                navigator.pop()
            }
        ) {
            Text("Fourth Screen")
        }
    }
}