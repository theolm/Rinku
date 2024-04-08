@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.theolm.rinku.compose.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.Rinku.consumeDeepLink
import dev.theolm.rinku.Rinku.deepLinkFlow

@Composable
fun DeepLinkListener(listener: (DeepLink) -> Unit) {
    val deepLinkState by deepLinkFlow.collectAsState()
    LaunchedEffect(deepLinkState) {
        consumeDeepLink()?.let {
            listener.invoke(it)
        }
    }
}
