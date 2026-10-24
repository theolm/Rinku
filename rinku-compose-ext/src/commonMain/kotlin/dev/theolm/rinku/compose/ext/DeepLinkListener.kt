package dev.theolm.rinku.compose.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.listenForDeepLinks

@Composable
fun DeepLinkListener(listener: (DeepLink) -> Unit) {
    val currentListener by rememberUpdatedState(listener)
    LaunchedEffect(Unit) {
        listenForDeepLinks { currentListener(it) }
    }
}
