package dev.theolm.rinku

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer

@Composable
fun ComponentActivity.Rinku(content: @Composable () -> Unit) {
    intent.dataString?.let {
        println("Initial deeplink: $it")
        Rinku.handleDeepLink(it)
    }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> {
            println("Received new intent: $it")
            it?.dataString?.let {
                Rinku.handleDeepLink(it)
            }
        }

        this@Rinku.addOnNewIntentListener(listener)
        onDispose { this@Rinku.removeOnNewIntentListener(listener) }
    }

    content()
}