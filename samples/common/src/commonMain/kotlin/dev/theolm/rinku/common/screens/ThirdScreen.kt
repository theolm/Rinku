package dev.theolm.rinku.common.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.theolm.rinku.common.components.DemoScaffold

@Composable
fun ThirdScreen(onBackPress: () -> Unit) {
    DemoScaffold(
        title = "Third Screen",
        onBackPress = onBackPress
    ) {
        Text("Third Screen")
    }
}
