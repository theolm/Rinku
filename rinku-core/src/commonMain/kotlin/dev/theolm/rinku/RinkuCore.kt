@file:Suppress("MatchingDeclarationName")

package dev.theolm.rinku

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

object Rinku {
    private val rinkuScope = MainScope()
    internal var deepLinkState = MutableStateFlow<DeepLink?>(null)
        private set

    fun handleDeepLink(url: String) {
        rinkuScope.launch {
            deepLinkState.emit(DeepLink(url))
        }
    }

    internal fun consumeDeepLink(): DeepLink? = deepLinkState.getAndUpdate { null }

}

suspend fun listenForDeepLinks(listener: (DeepLink) -> Unit) {
    Rinku.deepLinkState.filterNotNull().collect {
        Rinku.consumeDeepLink()?.let(listener)
    }
}
