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

    /**
     * Handle a deep link
     * @param url The deep link to handle
     */
    fun handleDeepLink(url: String) {
        rinkuScope.launch {
            deepLinkFlow.emit(DeepLink(url))
        }
    }

    /**
     * Handle a deep link with optional filter and mapper
     * @param url The deep link to handle
     * @param applyFilter Whether to apply the filter before handling the deep link
     * @param applyMapper Whether to apply the mapper before handling the deep link
     * @throws IllegalStateException If the filter or mapper is not set
     */
    fun handleDeepLink(url: String, applyFilter: Boolean = false, applyMapper: Boolean = false) {
        if (applyFilter) {
            deepLinkFilter?.let {
                if (!it.isValid(url)) return
            } ?: run {
                throw IllegalStateException("DeepLinkFilter not set")
            }
        }

        if (applyMapper) {
            deepLinkMapper?.let {
                handleDeepLink(it.map(url))
            } ?: run {
                throw IllegalStateException("DeepLinkMapper not set")
            }
        } else {
            handleDeepLink(url)
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

    internal fun setDeepLinkFilter(deepLinkFilter: DeepLinkFilter) {
        this.deepLinkFilter = deepLinkFilter
    }

    internal fun setDeepLinkMapper(deepLinkMapper: DeepLinkMapper) {
        this.deepLinkMapper = deepLinkMapper
    }
}

suspend fun listenForDeepLinks(listener: (DeepLink) -> Unit) {
    Rinku.deepLinkFlow.filterNotNull().collect {
        Rinku.consumeDeepLink()?.let(listener)
    }
}
