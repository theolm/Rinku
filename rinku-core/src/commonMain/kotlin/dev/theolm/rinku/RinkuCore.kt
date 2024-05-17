@file:Suppress("MatchingDeclarationName")

package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import dev.theolm.rinku.models.DeepLinkParam
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

object Rinku {
    private var deepLinkFilter: DeepLinkFilter? = null
    private var deepLinkMapper: DeepLinkMapper? = null

    private val rinkuScope = MainScope()
    internal var deepLinkFlow = MutableStateFlow<DeepLink?>(null)
        private set

    fun handleDeepLink(url: String) {
        rinkuScope.launch {
            deepLinkFlow.emit(DeepLink(url))
        }
    }

    internal fun consumeDeepLink(): DeepLink? = deepLinkFlow.getAndUpdate { null }

    fun buildUrl(
        schema: String,
        host: String,
        path: String? = null,
        vararg parameters: DeepLinkParam<*>
    ) = UrlBuilder.buildUrl(schema, host, path, *parameters)

    fun buildUrl(url: String, vararg parameters: DeepLinkParam<*>) =
        UrlBuilder.buildUrl(url, *parameters)

    fun setDeepLinkFilter(deepLinkFilter: DeepLinkFilter) {
        this.deepLinkFilter = deepLinkFilter
    }

    fun setDeepLinkMapper(deepLinkMapper: DeepLinkMapper) {
        this.deepLinkMapper = deepLinkMapper
    }
}

suspend fun listenForDeepLinks(listener: (DeepLink) -> Unit) {
    Rinku.deepLinkFlow.filterNotNull().collect {
        Rinku.consumeDeepLink()?.let(listener)
    }
}
