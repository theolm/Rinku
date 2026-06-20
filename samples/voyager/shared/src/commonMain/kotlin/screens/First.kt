package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.common.screens.FirstScreen

class First(
    private val deepLink: DeepLink? = null,
) : Screen {
    @Composable
    override fun Content() {
        FirstScreen(deepLink)
    }
}
