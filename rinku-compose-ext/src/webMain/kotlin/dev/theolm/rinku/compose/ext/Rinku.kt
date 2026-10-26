package dev.theolm.rinku.compose.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import dev.theolm.rinku.RinkuWeb
import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper

/**
 * Owns the browser adapter for this composition. Use once at the root, instead of manually
 * starting [RinkuWeb]. Ordinary recomposition does not recapture the initial URL.
 * Changing capture/filter/mapper configuration replaces the adapter and captures again
 * if enabled. Updating [onError] alone does not restart it.
 */
@Composable
fun Rinku(
    captureInitialUrl: Boolean = true,
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
    onError: (Exception) -> Unit = { it.printStackTrace() },
    content: @Composable () -> Unit,
) {
    val currentOnError = rememberUpdatedState(onError)
    val adapter = remember(captureInitialUrl, deepLinkFilter, deepLinkMapper) {
        RinkuWeb(captureInitialUrl, deepLinkFilter, deepLinkMapper) { currentOnError.value(it) }
    }
    DisposableEffect(adapter) {
        adapter.start()
        onDispose { adapter.close() }
    }
    content()
}
