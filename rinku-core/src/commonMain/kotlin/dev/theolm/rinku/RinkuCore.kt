package dev.theolm.rinku

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


object Rinku {
    internal var deepLinkState by mutableStateOf<DeepLink?>(null)
        private set

    fun handleDeepLink(url: String) {
        deepLinkState = DeepLink(url)
    }

    internal fun consumeDeepLink(): DeepLink? =
        deepLinkState?.also {
            deepLinkState = null
        }
}

@Composable
fun DeepLinkListener(listener: (DeepLink) -> Unit) {
    LaunchedEffect(Rinku.deepLinkState) {
        Rinku.consumeDeepLink()?.let {
            listener.invoke(it)
        }
    }
}