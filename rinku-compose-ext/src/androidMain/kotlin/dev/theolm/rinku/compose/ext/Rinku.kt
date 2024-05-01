package dev.theolm.rinku.compose.ext

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import dev.theolm.rinku.Rinku.handleDeepLink
import dev.theolm.rinku.models.DeepLinkFilter

@Composable
fun ComponentActivity.Rinku(deepLinkFilter: DeepLinkFilter? = null, content: @Composable () -> Unit) {
    intent.dataString?.let {
        val shouldFire = deepLinkFilter?.isValid(it) ?: true
        if (shouldFire) {
            handleDeepLink(it)
        }
    }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> {
            it?.dataString?.let {
                val shouldFire = deepLinkFilter?.isValid(it) ?: true
                if (shouldFire) {
                    handleDeepLink(it)
                }
            }
        }

        this@Rinku.addOnNewIntentListener(listener)
        onDispose { this@Rinku.removeOnNewIntentListener(listener) }
    }

    content()
}
