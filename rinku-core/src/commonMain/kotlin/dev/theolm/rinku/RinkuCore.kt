@file:Suppress("MatchingDeclarationName")

package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkParam
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

object Rinku {
    private val rinkuScope = MainScope()
    internal var deepLinkFlow = MutableStateFlow<DeepLink?>(null)
        private set

    fun handleDeepLink(url: String) {
        rinkuScope.launch {
            deepLinkFlow.emit(DeepLink(url))
        }
    }

    internal fun consumeDeepLink(): DeepLink? = deepLinkFlow.getAndUpdate { null }

    fun buildUri(url: String, vararg parameters: DeepLinkParam<*>): String {
        val uriBuilder = StringBuilder(url)
        if (parameters.isNotEmpty()) {
            uriBuilder.append("?")
            uriBuilder.append(parameters.joinToString("&") { "${it.name}=${it.serialize()}" })
        }
        return uriBuilder.toString()
    }
}

suspend fun listenForDeepLinks(listener: (DeepLink) -> Unit) {
    Rinku.deepLinkFlow.filterNotNull().collect {
        Rinku.consumeDeepLink()?.let(listener)
    }
}
