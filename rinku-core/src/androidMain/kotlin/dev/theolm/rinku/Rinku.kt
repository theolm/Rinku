package dev.theolm.rinku

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer

@Composable
fun ComponentActivity.Rinku(content: @Composable () -> Unit) {
    DisposableEffect(Unit) {
        val listener = Consumer<Intent> {
            it?.dataString?.let {
                Rinku.handleDeepLink(it)
            }
        }

        this@Rinku.addOnNewIntentListener(listener)
        onDispose { this@Rinku.removeOnNewIntentListener(listener) }
    }

    content()
}