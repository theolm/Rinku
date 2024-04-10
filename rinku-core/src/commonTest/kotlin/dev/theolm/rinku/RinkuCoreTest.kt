package dev.theolm.rinku

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class RinkuCoreTest {
    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Ensuring Rinku is reset before each test
        Rinku.consumeDeepLink() // Consume any existing deep link to reset
    }

    @AfterTest
    fun teardownDispatchers() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handleDeepLink sets deepLinkState`() = runTest(testDispatcher) {
        val testUrl = "https://test.com"
        Rinku.handleDeepLink(testUrl)
        advanceUntilIdle()

        val consumedLink = Rinku.consumeDeepLink()
        assertEquals(
            expected = testUrl,
            actual = consumedLink?.data,
            message = "Consumed deep link did not match."
        )
    }

    @Test
    fun `consumeDeepLink returns and clears deepLinkState`() = runTest(testDispatcher) {
        val testUrl = "https://test.com"
        Rinku.handleDeepLink(testUrl)
        advanceUntilIdle()

        // Consume the deep link
        val consumedLink = Rinku.consumeDeepLink()
        assertNotNull(
            actual = consumedLink,
            message = "Consumed deep link was null."
        )

        // Ensure deepLinkState is cleared
        assertNull(
            actual = Rinku.consumeDeepLink(),
            message = "deepLinkState was not cleared after consumption."
        )
    }

    @Test
    fun `consumeDeepLink returns null when no deepLink is set`() = runTest(testDispatcher) {
        advanceUntilIdle()

        assertNull(
            actual = Rinku.consumeDeepLink(),
            message = "consumeDeepLink should return null when no deepLink is set."
        )
    }

    @Test
    fun `on handleDeepLink expects listenForDeepLinks receive a deeplink`() =
        runTest(testDispatcher) {
            val testUrl = "https://test.com"
            var receivedDeepLink: DeepLink? = null

            val job = launch {
                listenForDeepLinks { receivedDeepLink = it }
            }

            Rinku.handleDeepLink(testUrl)
            advanceUntilIdle()
            job.cancel()

            assertEquals(
                expected = testUrl,
                actual = receivedDeepLink?.data,
                message = "Received deep link did not match."
            )
        }
}
