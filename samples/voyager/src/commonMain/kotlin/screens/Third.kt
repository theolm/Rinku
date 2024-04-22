package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.theolm.rinku.common.models.RandomArgument
import dev.theolm.rinku.common.screens.ThirdScreen

class Third(private val randomArgument: List<RandomArgument> = emptyList()) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ThirdScreen(
            randomArgument = randomArgument,
            onBackPress = {
                navigator.pop()
            }
        )
    }
}
