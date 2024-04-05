package screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import components.DemoScaffold



    @Composable
    fun FourthScreen(onBackPress: () -> Unit) {
        DemoScaffold(
            title = "Fourth",
            onBackPress = onBackPress
        ) {
            Text("Fourth Screen")
        }
    }
