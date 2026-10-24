package dev.theolm.rinku

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlatformDeepLinkTest {
    @BeforeTest
    @AfterTest
    fun clearPendingLinks() {
        while (Rinku.consumeDeepLink() != null) { /* Drain pending events between tests. */ }
    }

    @Test
    fun `startup links are delivered in order including repeated URLs`() = runTest {
        val urls = listOf("rinku://first", "rinku://first", "rinku://second")
        urls.forEach { assertTrue(Rinku.enqueuePlatformDeepLink(it)) }
        val received = mutableListOf<String>()
        val job = backgroundScope.launch { listenForDeepLinks { received += it.data } }
        runCurrent()
        assertEquals(urls, received)
        job.cancel()
    }

    @Test
    fun `active listener receives later events`() = runTest {
        val received = mutableListOf<String>()
        backgroundScope.launch { listenForDeepLinks { received += it.data } }
        runCurrent()
        Rinku.enqueuePlatformDeepLink("rinku://first")
        runCurrent()
        Rinku.enqueuePlatformDeepLink("rinku://second")
        runCurrent()
        assertEquals(listOf("rinku://first", "rinku://second"), received)
    }

    @Test
    fun `overflow rejects newest link without replacing pending links`() {
        repeat(64) { assertTrue(Rinku.enqueuePlatformDeepLink("rinku://item/$it")) }
        assertFalse(Rinku.enqueuePlatformDeepLink("rinku://overflow"))
        assertEquals((0 until 64).map { "rinku://item/$it" },
            generateSequence { Rinku.consumeDeepLink()?.data }.toList())
    }
}
