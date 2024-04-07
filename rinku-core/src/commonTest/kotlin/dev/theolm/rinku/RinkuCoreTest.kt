package dev.theolm.rinku

import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.DefaultAsserter.assertNull
import kotlin.test.Test

class RinkuCoreTest {

    @BeforeTest
    fun setUp() {
        // Ensuring Rinku is reset before each test
        Rinku.consumeDeepLink() // Consume any existing deep link to reset
    }

    @Test
    fun `handleDeepLink sets deepLinkState`() {
        val testUrl = "https://test.com"
        Rinku.handleDeepLink(testUrl)

        assertEquals("Deep link state was not set correctly.", DeepLink(testUrl), Rinku.consumeDeepLink())
    }

    @Test
    fun `consumeDeepLink returns and clears deepLinkState`() {
        val testUrl = "https://test.com"
        Rinku.handleDeepLink(testUrl)

        // Consume the deep link
        val consumedLink = Rinku.consumeDeepLink()
        assertEquals("Consumed deep link did not match.", DeepLink(testUrl), consumedLink)

        // Ensure deepLinkState is cleared
        assertNull("deepLinkState was not cleared after consumption.", Rinku.consumeDeepLink())
    }

    @Test
    fun `consumeDeepLink returns null when no deepLink is set`() {
        assertNull("consumeDeepLink should return null when no deepLink is set.", Rinku.consumeDeepLink())
    }
}