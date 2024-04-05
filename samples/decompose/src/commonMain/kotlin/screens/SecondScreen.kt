package screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import components.DemoScaffold


@Composable
fun SecondScreen(onBackPress: () -> Unit) {

    DemoScaffold(
        title = "Second",
        onBackPress = onBackPress
    ) {
        Text("Second Screen")
    }
}
