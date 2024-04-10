package dev.theolm.rinku.common.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.Rinku
import dev.theolm.rinku.common.components.DemoScaffold
import kotlin.random.Random

@Composable
fun FirstScreen(deepLink: DeepLink? = null) {
    DemoScaffold(
        title = "Sample"
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    Rinku.handleDeepLink(randomDeepLink())
                }
            ) {
                Text("Random deeplink")
            }
            Spacer(modifier = Modifier.height(32.dp))
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

private fun randomDeepLink(): String {
    val paths = mutableListOf("second", "third", "fourth")
    val first = paths.random().also { paths.remove(it) }
    val second = paths.random().also { paths.remove(it) }
    val third = paths.first()

    val arguments = "a=${Random.nextInt()}&b=${Random.nextInt()}&c=${Random.nextInt()}"
    return "rinku://dev.theolm.rinku/$first/$second/$third/?$arguments"
}
