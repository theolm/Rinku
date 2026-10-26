package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.w3c.dom.PopStateEvent
import org.w3c.dom.events.Event
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RinkuWebTest {
    private val originalUrl = window.location.href
    private val adapters = mutableListOf<RinkuWeb>()

    @BeforeTest
    fun prepare() {
        drain()
        window.history.replaceState(null, "", "/rinku-test/")
    }

    @AfterTest
    fun cleanup() {
        adapters.forEach { it.close() }
        drain()
        window.history.replaceState(null, "", originalUrl)
    }

    @Test
    fun initialUrlPreservesOriginPrefixQueryAndFragment() {
        window.history.replaceState(null, "", "/app/second/a%20b?name=hello%20world#section")
        adapter().start()
        val link = assertNotNull(Rinku.consumeDeepLink())
        assertEquals(window.location.href, link.data)
        assertEquals(listOf("app", "second", "a b"), link.pathSegments)
        assertEquals("hello world", link.parameters["name"])
        assertNull(Rinku.consumeDeepLink())
    }

    @Test
    fun rootIsDeliveredAndInitialCaptureCanBeDisabled() {
        window.history.replaceState(null, "", "/")
        val first = adapter()
        first.start()
        assertEquals(window.location.href, Rinku.consumeDeepLink()?.data)
        first.close()
        adapter(captureInitialUrl = false).start()
        assertNull(Rinku.consumeDeepLink())
    }

    @Test
    fun repeatedStartDoesNotReplayAndCloseRemovesListener() {
        val adapter = adapter()
        adapter.start()
        adapter.start()
        assertNotNull(Rinku.consumeDeepLink())
        assertNull(Rinku.consumeDeepLink())
        adapter.close()
        adapter.close()
        popState()
        assertNull(Rinku.consumeDeepLink())
        assertFailsWith<IllegalStateException> { adapter.start() }
    }

    @Test
    fun onlyOneAdapterCanBeActiveAndClosingRejectedAdapterDoesNotReleaseOwner() {
        val first = adapter(captureInitialUrl = false)
        val second = adapter(captureInitialUrl = false)
        first.start()
        assertFailsWith<IllegalStateException> { second.start() }
        second.close()
        val third = adapter(captureInitialUrl = false)
        assertFailsWith<IllegalStateException> { third.start() }
        first.close()
        third.start()
        popState()
        assertNotNull(Rinku.consumeDeepLink())
        assertNull(Rinku.consumeDeepLink())
    }

    @Test
    fun filterPrecedesMapperAndRejectedLinksAreNotMapped() {
        val calls = mutableListOf<String>()
        adapter(
            filter = object : DeepLinkFilter {
                override fun isValid(url: String): Boolean {
                    calls += "filter"
                    return !url.contains("reject")
                }
            },
            mapper = object : DeepLinkMapper {
                override fun map(url: String): String {
                    calls += "mapper"
                    return url.replace("/app/", "/")
                }
            },
        ).start()
        drain()
        calls.clear()
        window.history.replaceState(null, "", "/app/second")
        popState()
        assertEquals(listOf("filter", "mapper"), calls)
        assertEquals(listOf("second"), Rinku.consumeDeepLink()?.pathSegments)
        calls.clear()
        window.history.replaceState(null, "", "/reject")
        popState()
        assertEquals(listOf("filter"), calls)
        assertNull(Rinku.consumeDeepLink())
    }

    @Test
    fun writesAndHashEventsAreNotObservedButDistinctPopEventsAreRetained() {
        adapter(captureInitialUrl = false).start()
        window.history.pushState(null, "", "/second?x=1")
        window.history.replaceState(null, "", "/third#section")
        window.dispatchEvent(Event("hashchange"))
        assertNull(Rinku.consumeDeepLink())
        repeat(2) { popState() }
        assertEquals(window.location.href, Rinku.consumeDeepLink()?.data)
        assertEquals(window.location.href, Rinku.consumeDeepLink()?.data)
        assertNull(Rinku.consumeDeepLink())
    }

    @Test
    fun pendingLinksSurviveCloseAndReachSharedListenerInOrder() = runTest {
        val adapter = adapter()
        adapter.start()
        val initial = window.location.href
        window.history.replaceState(null, "", "/second")
        popState()
        adapter.close()
        val received = mutableListOf<String>()
        backgroundScope.launch { listenForDeepLinks { received += it.data } }
        runCurrent()
        assertEquals(listOf(initial, window.location.href), received)
    }

    @Test
    fun overflowReportsErrorAndRetainsFirst64Links() {
        val errors = mutableListOf<Exception>()
        adapter(captureInitialUrl = false, onError = errors::add).start()
        repeat(65) { index ->
            window.history.replaceState(null, "", "/item/$index")
            popState()
        }
        assertEquals(1, errors.size)
        assertTrue(errors.single().message.orEmpty().contains("queue is full"))
        assertEquals((0 until 64).map { "item/$it" },
            generateSequence { Rinku.consumeDeepLink()?.pathSegments?.joinToString("/") }.toList())
    }

    @Test
    fun filterAndMapperErrorsAreReportedAndLaterEventsStillWork() {
        val errors = mutableListOf<Exception>()
        var failFilter = true
        var failMapper = true
        adapter(
            filter = object : DeepLinkFilter {
                override fun isValid(url: String): Boolean {
                    check(!failFilter) { "filter failure" }
                    return true
                }
            },
            mapper = object : DeepLinkMapper {
                override fun map(url: String): String {
                    check(!failMapper) { "mapper failure" }
                    return url
                }
            },
            onError = errors::add,
        ).start()
        failFilter = false
        popState()
        failMapper = false
        popState()
        assertEquals(listOf("filter failure", "mapper failure"), errors.map { it.message })
        assertNotNull(Rinku.consumeDeepLink())
        assertNull(Rinku.consumeDeepLink())
    }

    @Test
    fun realBackAndForwardReceiveCurrentUrl() = runTest {
        adapter(captureInitialUrl = false).start()
        window.history.pushState(null, "", "/history/first")
        window.history.pushState(null, "", "/history/second?q=2")
        traverse { window.history.back() }
        assertEquals(listOf("history", "first"), Rinku.consumeDeepLink()?.pathSegments)
        traverse { window.history.forward() }
        val link = assertNotNull(Rinku.consumeDeepLink())
        assertEquals(listOf("history", "second"), link.pathSegments)
        assertEquals("2", link.parameters["q"])
        assertNull(Rinku.consumeDeepLink())
    }

    @Test
    fun replacementWithoutConfigurationClearsRegisteredFilterAndMapper() {
        val configured = adapter(
            captureInitialUrl = false,
            filter = object : DeepLinkFilter {
                override fun isValid(url: String) = false
            },
            mapper = object : DeepLinkMapper {
                override fun map(url: String) = "https://example.com/old"
            },
        )
        configured.start()
        configured.close()
        adapter(captureInitialUrl = false).start()

        // Explicit opt-in must fail when the current adapter has no configuration,
        // rather than silently invoking callbacks retained from the previous adapter.
        assertFailsWith<IllegalStateException> {
            Rinku.handleDeepLink(window.location.href, applyFilter = true)
        }
        assertFailsWith<IllegalStateException> {
            Rinku.handleDeepLink(window.location.href, applyMapper = true)
        }
        popState()
        assertEquals(window.location.href, Rinku.consumeDeepLink()?.data)
        assertNull(Rinku.consumeDeepLink())
    }

    private suspend fun traverse(action: () -> Unit) {
        val event = CompletableDeferred<Unit>()
        val listener: (Event) -> Unit = { event.complete(Unit) }
        window.addEventListener("popstate", listener)
        try {
            action()
            withContext(Dispatchers.Default) { withTimeout(5_000) { event.await() } }
        } finally {
            window.removeEventListener("popstate", listener)
        }
    }

    private fun adapter(
        captureInitialUrl: Boolean = true,
        filter: DeepLinkFilter? = null,
        mapper: DeepLinkMapper? = null,
        onError: (Exception) -> Unit = { throw it },
    ) = RinkuWeb(captureInitialUrl, filter, mapper, onError).also { adapters += it }

    private fun popState() {
        assertTrue(window.dispatchEvent(PopStateEvent("popstate")))
    }

    private fun drain() {
        while (Rinku.consumeDeepLink() != null) { /* Drain the shared queue between tests. */ }
    }
}
