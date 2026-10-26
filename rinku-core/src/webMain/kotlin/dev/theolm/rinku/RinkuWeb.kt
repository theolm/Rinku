package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * Passive browser adapter for Kotlin/JS and Kotlin/Wasm. Call [start] once at application
 * startup and [close] when the application is disposed. Only one adapter may be active.
 *
 * Receives the full initial URL (unless disabled) and `popstate` events. Does not write
 * history, intercept `pushState`/`replaceState`, or interpret fragments as routes.
 * Filtering precedes mapping; accepted links use the shared bounded FIFO queue.
 * Navigation and deployment-prefix normalization remain the application's responsibility.
 */
class RinkuWeb(
    private val captureInitialUrl: Boolean = true,
    private val deepLinkFilter: DeepLinkFilter? = null,
    private val deepLinkMapper: DeepLinkMapper? = null,
    private val onError: (Exception) -> Unit = { it.printStackTrace() },
) {
    private var started = false
    private var closed = false
    private val popStateListener: (Event) -> Unit = { receiveCurrentUrl() }

    /** Repeated starts are a no-op. A closed adapter cannot be restarted. */
    fun start() {
        check(!closed) { "RinkuWeb is closed" }
        if (started) return
        check(activeAdapter == null) { "A RinkuWeb adapter is already active" }
        activeAdapter = this
        started = true
        try {
            window.addEventListener("popstate", popStateListener)
            deepLinkFilter?.let { Rinku.setDeepLinkFilter(it) }
            deepLinkMapper?.let { Rinku.setDeepLinkMapper(it) }
            if (captureInitialUrl) receiveCurrentUrl()
        } catch (error: Exception) {
            close()
            throw error
        }
    }

    private fun receiveCurrentUrl() {
        if (!started || closed) return
        try {
            val url = window.location.href
            if (deepLinkFilter?.isValid(url) == false) return
            val mapped = deepLinkMapper?.map(url) ?: url
            if (closed) return
            check(Rinku.enqueuePlatformDeepLink(mapped)) {
                "Rinku web queue is full (64 pending links); newest link rejected"
            }
        } catch (error: Exception) {
            onError(error)
        }
    }

    /** Removes browser listeners without discarding links already in the shared queue. */
    fun close() {
        if (closed) return
        closed = true
        try {
            if (started) window.removeEventListener("popstate", popStateListener)
        } finally {
            if (activeAdapter === this) activeAdapter = null
        }
    }

    private companion object {
        var activeAdapter: RinkuWeb? = null
    }
}
