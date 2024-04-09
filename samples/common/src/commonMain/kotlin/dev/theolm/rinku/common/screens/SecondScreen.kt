package dev.theolm.rinku.common.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.theolm.rinku.common.components.DemoScaffold

@Composable
fun SecondScreen(onBackPress: () -> Unit) {
    DemoScaffold(
        title = "Second Screen",
        onBackPress = onBackPress
    ) {
        Text("Second Screen")
    }
}
