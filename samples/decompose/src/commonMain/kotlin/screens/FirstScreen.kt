package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.DemoScaffold
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.Rinku

@Composable
fun FirstScreen(deepLink: DeepLink? = null) {
    DemoScaffold(
        title = "Decompose sample"
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                Rinku.handleDeepLink("rinku://dev.theolm.rinku/fourth/second/third/")
            }) {
                Text("Internal deeplink")
            }
            deepLink?.let {
                Column {
                    Text("URI: ${it.uri}")
                    Text("host: ${it.host}")
                    Text("Path: ${it.path}")
                    Text("Path List: ${it.pathSegments}")
                    Text("Query: ${it.query}")
                    Text("encoded Query: ${it.encodedQuery}")
                    Text("Query map: ${it.parameters}")
                }
            } ?: run {
                Text("No deep link received")
            }
        }
    }
}
