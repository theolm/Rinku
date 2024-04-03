package home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.DeepLinkListener
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        Screen()
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun Screen() {
        val screenModel = rememberScreenModel { HomeScreenModel() }
        var uiState by screenModel.uiState

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    uiState = uiState.copy(showContent = !uiState.showContent)
                }
            ) {
                Text("Click me!")
            }
            AnimatedVisibility(uiState.showContent) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painterResource(DrawableResource("compose-multiplatform.xml")), null)
                    Text("Compose: ${uiState.message}")
                }
            }

            var deepLink by remember { mutableStateOf<DeepLink?>(null) }
            DeepLinkListener {
                deepLink = it
            }
            Text("Deep link: $deepLink")
        }
    }
}