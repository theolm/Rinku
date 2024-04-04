package home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import components.DemoScaffold
import dev.theolm.rinku.DeepLinkListener

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { HomeScreenModel() }
        val uiState by screenModel.uiState

        DemoScaffold(
            title = "Home"
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                uiState.deepLink?.let {
                    Text("URI: ${it.uri}")
                    Text("host: ${it.host}")
                    Text("Path: ${it.path}")
                    Text("Path List: ${it.pathSegments}")
                    Text("Query: ${it.query}")
                    Text("encoded Query: ${it.encodedQuery}")
                    Text("Query map: ${it.parameters}")
                } ?: run {
                    Text("No deep link received")
                }

            }
        }

        DeepLinkListener {
            screenModel.onDeepLinkReceived(it)
        }
    }
}