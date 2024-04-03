package dev.theolm.rinku

import androidx.compose.runtime.staticCompositionLocalOf

val LocalDeepLink = staticCompositionLocalOf<DeepLink?> { null }

data class DeepLink(
    val uri: String,
)
