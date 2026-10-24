package dev.theolm.rinku

import dev.theolm.rinku.desktop.DesktopInstance
import dev.theolm.rinku.desktop.OpenUriHandler
import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import java.net.URI
import java.util.Locale

/**
 * JVM desktop entry point. Register URL schemes in the application package/installer.
 * Call [start] before creating windows and [close] when the application exits.
 * Use one adapter per application process. Navigation remains in the shared listeners.
 *
 * Only [schemes] are accepted from arguments, native events and IPC. Filter and mapper
 * run on the receiving thread; listeners run in their collecting coroutine's context.
 */
class RinkuDesktop(
    applicationId: String,
    schemes: Set<String>,
    private val deepLinkFilter: DeepLinkFilter? = null,
    private val deepLinkMapper: DeepLinkMapper? = null,
    private val onError: (Exception) -> Unit = { it.printStackTrace() },
) : AutoCloseable {
    enum class StartResult { PRIMARY, FORWARDED }

    private val schemes = schemes.map { it.lowercase(Locale.ROOT) }.toSet()
    private val instance = DesktopInstance(applicationId, ::onDeepLinkReceived, onError)
    private var openUriHandler: AutoCloseable? = null
    private var result: StartResult? = null
    @Volatile
    private var closed = false

    init {
        require(this.schemes.isNotEmpty()) { "At least one URL scheme is required" }
        require(this.schemes.all { SCHEME.matches(it) }) { "Invalid URL scheme" }
    }

    /**
     * A secondary process forwards its links and returns [StartResult.FORWARDED].
     * The caller must then return from main without opening a window.
     * Throws on startup/forwarding failure rather than silently opening another instance.
     * Repeated calls return the original result without processing arguments again.
     */
    @Synchronized
    fun start(args: Array<String> = emptyArray()): StartResult {
        check(!closed) { "RinkuDesktop is closed" }
        result?.let { return it }
        val links = args.filter(::accepts)
        require(links.size <= DesktopInstance.MAX_LINKS) { "Too many startup links" }
        try {
            val primary = instance.start(links) {
                deepLinkFilter?.let { Rinku.setDeepLinkFilter(it) }
                deepLinkMapper?.let { Rinku.setDeepLinkMapper(it) }
                links.forEach(::onDeepLinkReceived)
                openUriHandler = OpenUriHandler.install(::onDeepLinkReceived)
            }
            if (!primary) {
                return StartResult.FORWARDED.also { result = it }
            }
            return StartResult.PRIMARY.also { result = it }
        } catch (error: Exception) {
            close()
            throw error
        }
    }

    /** Also usable by applications with their own native URL-event integration. */
    fun onDeepLinkReceived(url: String) {
        if (closed || !accepts(url)) return
        try {
            if (deepLinkFilter?.isValid(url) == false) return
            val mapped = deepLinkMapper?.map(url) ?: url
            check(Rinku.enqueuePlatformDeepLink(mapped)) {
                "Rinku desktop queue is full (64 pending links); newest link rejected"
            }
        } catch (error: Exception) {
            onError(error)
        }
    }

    private fun accepts(url: String): Boolean {
        if (url.length > DesktopInstance.MAX_URL_LENGTH) return false
        return try {
            URI(url).scheme?.lowercase(Locale.ROOT) in schemes
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: java.net.URISyntaxException) {
            false
        }
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        try {
            openUriHandler?.close()
        } finally {
            instance.close()
        }
    }

    private companion object {
        val SCHEME = Regex("[a-z][a-z0-9+.-]*")
    }
}
