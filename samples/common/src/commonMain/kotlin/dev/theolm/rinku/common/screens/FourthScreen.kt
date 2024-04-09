package dev.theolm.rinku.common.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.theolm.rinku.common.components.DemoScaffold

@Composable
fun FourthScreen(onBackPress: () -> Unit) {
    DemoScaffold(
        title = "Fourth Screen",
        onBackPress = onBackPress
    ) {
        Text("Fourth Screen")
    }
}
