package dev.theolm.rinku.compose.ext

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import dev.theolm.rinku.Rinku.handleDeepLink

@Composable
fun ComponentActivity.Rinku(content: @Composable () -> Unit) {
    intent.dataString?.let {
        handleDeepLink(it)
    }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> {
            it?.dataString?.let {
                handleDeepLink(it)
            }
        }

        this@Rinku.addOnNewIntentListener(listener)
        onDispose { this@Rinku.removeOnNewIntentListener(listener) }
    }

    content()
}
