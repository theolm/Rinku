package screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import components.DemoScaffold

@Composable
fun ThirdScreen(onBackPress: () -> Unit) {
    DemoScaffold(
        title = "Third",
        onBackPress = onBackPress
    ) {
        Text("Third Screen")
    }
}
