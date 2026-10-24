package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class RinkuDesktopTest {
    @BeforeTest
    @AfterTest
    fun clearPendingLinks() {
        while (Rinku.consumeDeepLink() != null) { /* Drain pending events between tests. */ }
    }

    @Test
    fun `only configured schemes and valid URIs are accepted`() {
        RinkuDesktop("test", setOf("Rinku")).use { desktop ->
            listOf("--debug", "other://first", "rinku://bad path").forEach(desktop::onDeepLinkReceived)
            assertNull(Rinku.consumeDeepLink())
            desktop.onDeepLinkReceived("RINKU://first?value=a%20b")
            assertEquals("RINKU://first?value=a%20b", Rinku.consumeDeepLink()?.data)
        }
    }

    @Test
    fun `filter runs before mapper and rejected links are not mapped`() {
        val mapped = mutableListOf<String>()
        val filter = object : DeepLinkFilter {
            override fun isValid(url: String) = !url.endsWith("blocked")
        }
        val mapper = object : DeepLinkMapper {
            override fun map(url: String): String {
                mapped += url
                return url.replace("external", "internal")
            }
        }
        RinkuDesktop("test", setOf("rinku"), filter, mapper).use { desktop ->
            desktop.onDeepLinkReceived("rinku://blocked")
            desktop.onDeepLinkReceived("rinku://external")
            assertEquals(listOf("rinku://external"), mapped)
            assertEquals("rinku://internal", Rinku.consumeDeepLink()?.data)
        }
    }

    @Test
    fun `closed adapter ignores events and cannot restart`() {
        val desktop = RinkuDesktop("test", setOf("rinku"))
        desktop.close()
        desktop.close()
        desktop.onDeepLinkReceived("rinku://first")
        assertNull(Rinku.consumeDeepLink())
        assertFailsWith<IllegalStateException> { desktop.start() }
    }

    @Test
    fun `overflow and mapper failures are reported`() {
        val errors = mutableListOf<Exception>()
        RinkuDesktop("test", setOf("rinku"), onError = errors::add).use { desktop ->
            repeat(65) { desktop.onDeepLinkReceived("rinku://item/$it") }
            assertEquals(1, errors.size)
        }
        clearPendingLinks()
        val mapper = object : DeepLinkMapper {
            override fun map(url: String): String = error("Mapping failed")
        }
        RinkuDesktop("test", setOf("rinku"), deepLinkMapper = mapper, onError = errors::add).use {
            it.onDeepLinkReceived("rinku://first")
            assertEquals("Mapping failed", errors.last().message)
            assertNull(Rinku.consumeDeepLink())
        }
    }

    @Test
    fun `configuration requires an application id and valid schemes`() {
        assertFailsWith<IllegalArgumentException> { RinkuDesktop(" ", setOf("rinku")) }
        assertFailsWith<IllegalArgumentException> { RinkuDesktop("test", emptySet()) }
        assertFailsWith<IllegalArgumentException> { RinkuDesktop("test", setOf("bad scheme")) }
    }

    @Test
    fun `concurrent desktop events are queued without loss`() {
        RinkuDesktop("test", setOf("rinku")).use { desktop ->
            val threads = List(4) { source ->
                kotlin.concurrent.thread {
                    repeat(16) { desktop.onDeepLinkReceived("rinku://source/$source/$it") }
                }
            }
            threads.forEach { it.join(5_000) }
            val received = generateSequence { Rinku.consumeDeepLink()?.data }.toList()
            assertEquals(64, received.size)
            assertEquals(64, received.toSet().size)
        }
    }
}
