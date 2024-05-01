package dev.theolm.rinku.compose.ext

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import dev.theolm.rinku.Rinku.handleDeepLink
import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import dev.theolm.rinku.treatBeforeFire

@Composable
fun ComponentActivity.Rinku(
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
    content: @Composable () -> Unit
) {
    intent.dataString?.let {
        treatBeforeFire(
            deepLink = it,
            deepLinkFilter = deepLinkFilter,
            deepLinkMapper = deepLinkMapper,
            fireDeeplink = { handleDeepLink(it) }
        )
    }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> {
            it?.dataString?.let {
                treatBeforeFire(
                    deepLink = it,
                    deepLinkFilter = deepLinkFilter,
                    deepLinkMapper = deepLinkMapper,
                    fireDeeplink = { handleDeepLink(it) }
                )
            }
        }

        this@Rinku.addOnNewIntentListener(listener)
        onDispose { this@Rinku.removeOnNewIntentListener(listener) }
    }

    content()
}
