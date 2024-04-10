package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.theolm.rinku.common.screens.SecondScreen

class Second : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SecondScreen {
            navigator.pop()
        }
    }
}