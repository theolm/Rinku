@file:Suppress("MatchingDeclarationName")

package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import dev.theolm.rinku.models.DeepLinkParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

object Rinku {
    private var deepLinkFilter: DeepLinkFilter? = null
    private var deepLinkMapper: DeepLinkMapper? = null

    // Resolve Main when dispatching, not when the singleton is first accessed by an adapter.
    private val rinkuScope = CoroutineScope(SupervisorJob())
    internal var deepLinkFlow = MutableStateFlow<DeepLink?>(null)
        private set

    // Platform adapters may receive several links before a listener is installed.
    // Keep this separate from the existing mobile/internal single-value contract.
    private val pendingPlatformLinks = MutableStateFlow<List<DeepLink>>(emptyList())
    internal val hasPendingLinks = combine(deepLinkFlow, pendingPlatformLinks) { link, pending ->
        link != null || pending.isNotEmpty()
    }

    internal fun enqueuePlatformDeepLink(url: String): Boolean {
        val link = DeepLink(url)
        while (true) {
            val pending = pendingPlatformLinks.value
            if (pending.size >= MAX_PENDING_PLATFORM_LINKS) return false
            if (pendingPlatformLinks.compareAndSet(pending, pending + link)) return true
        }
    }

    /**
     * Handle a deep link
     * @param url The deep link to handle
     */
    fun handleDeepLink(url: String) {
        rinkuScope.launch(Dispatchers.Main) {
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

    internal fun consumeDeepLink(): DeepLink? =
        deepLinkFlow.getAndUpdate { null } ?: consumePlatformDeepLink()

    private fun consumePlatformDeepLink(): DeepLink? {
        while (true) {
            val pending = pendingPlatformLinks.value
            if (pending.isEmpty()) return null
            if (pendingPlatformLinks.compareAndSet(pending, pending.drop(1))) return pending.first()
        }
    }

    fun buildUrl(
        schema: String,
        host: String,
        path: String? = null,
        vararg parameters: DeepLinkParam<*>
    ) = UrlBuilder.buildUrl(schema, host, path, *parameters)

    fun buildUrl(url: String, vararg parameters: DeepLinkParam<*>) =
        UrlBuilder.buildUrl(url, *parameters)

    internal fun setDeepLinkFilter(deepLinkFilter: DeepLinkFilter?) {
        this.deepLinkFilter = deepLinkFilter
    }

    internal fun setDeepLinkMapper(deepLinkMapper: DeepLinkMapper?) {
        this.deepLinkMapper = deepLinkMapper
    }

    private const val MAX_PENDING_PLATFORM_LINKS = 64
}

/**
 * Consumes pending links in the collecting coroutine's context. Use one logical listener;
 * events are consumed once rather than broadcast. Desktop startup links retain FIFO order.
 */
suspend fun listenForDeepLinks(listener: (DeepLink) -> Unit) {
    Rinku.hasPendingLinks.filter { it }.collect {
        while (true) {
            // Check before every dequeue so a callback that cancels the collector
            // neither receives more links nor consumes them from the next listener.
            currentCoroutineContext().ensureActive()
            val link = Rinku.consumeDeepLink() ?: break
            listener(link)
        }
    }
}
