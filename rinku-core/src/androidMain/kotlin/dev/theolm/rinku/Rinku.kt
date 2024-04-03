package dev.theolm.rinku

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer

@Composable
fun ComponentActivity.Rinku(content: @Composable () -> Unit) {

    var deepLink by remember { mutableStateOf<DeepLink?>(null) }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> {
            deepLink = it?.dataString?.let {
                DeepLink(uri = it)
            }
        }

        this@Rinku.addOnNewIntentListener(listener)
        onDispose { this@Rinku.removeOnNewIntentListener(listener) }
    }

    CompositionLocalProvider(LocalDeepLink provides deepLink) {
        content()
    }
}