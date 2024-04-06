package screens

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import components.DemoScaffold
import dev.theolm.rinku.Rinku


@Composable
    fun FourthScreen(onBackPress: () -> Unit) {
        DemoScaffold(
            title = "Fourth",
            onBackPress = onBackPress
        ) {
            Text("Fourth Screen")

            Button(onClick = {
                Rinku.handleDeepLink("rinku://dev.theolm.rinku/fourth/second/third/second")
            }) {
                Text("New deeplink")
            }
        }
    }
