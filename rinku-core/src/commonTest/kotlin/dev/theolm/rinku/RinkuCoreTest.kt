package dev.theolm.rinku

import dev.theolm.fakes.FakeDeepLinkFilter
import dev.theolm.fakes.FakeDeepLinkMapper
import dev.theolm.models.ComplexModel
import dev.theolm.models.ExpectedMockComplexModel
import dev.theolm.models.Name
import dev.theolm.models.mockComplexModel
import dev.theolm.rinku.models.DeepLinkParam
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val TestUrl = "https://test.com"

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
        Rinku.handleDeepLink(TestUrl)
        advanceUntilIdle()

        val consumedLink = Rinku.consumeDeepLink()
        assertEquals(
            expected = TestUrl,
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
            var receivedDeepLink: DeepLink? = null

            val job = launch {
                listenForDeepLinks { receivedDeepLink = it }
            }

            Rinku.handleDeepLink(TestUrl)
            advanceUntilIdle()
            job.cancel()

            assertEquals(
                expected = TestUrl,
                actual = receivedDeepLink?.data,
                message = "Received deep link did not match."
            )
        }

    @Test
    fun `buildUri creates a deep link with the provided uri and parameters`() {
        val testModel = Name("Testing")
        val testParam = DeepLinkParam(
            "testParam",
            testModel,
            Name.serializer()
        )
        val url = Rinku.buildUrl(TestUrl, testParam)

        assertEquals(
            expected = "https://test.com?testParam={\"name\":\"Testing\"}",
            actual = url,
            message = "url does not match expected value."
        )
    }

    @Test
    fun `buildUri using a complex model creates a deep link with the provided uri and parameters `() {
        val testParam = DeepLinkParam(
            "complexParam",
            mockComplexModel,
            ComplexModel.serializer()
        )
        val url = Rinku.buildUrl(TestUrl, testParam)
        val expected = "https://test.com?complexParam=$ExpectedMockComplexModel"

        assertEquals(
            expected = expected,
            actual = url,
            message = "url does not match expected value."
        )
    }

    @Test
    fun `buildUri using schema host and a complex model creates a deep link with the provided url and parameterss `() {
        val schema = "https"
        val host = "test.com"
        val testParam = DeepLinkParam(
            "complexParam",
            mockComplexModel,
            ComplexModel.serializer()
        )
        val url = Rinku.buildUrl(
            schema = schema,
            host = host,
            path = null,
            testParam
        )
        val expected = "https://test.com?complexParam=$ExpectedMockComplexModel"

        assertEquals(
            expected = expected,
            actual = url,
            message = "url does not match expected value."
        )
    }

    @Test
    fun `on handleDeepLink expects with deeplinkMapper applied - expects the deeplink to have the correct url`() =
        runTest(testDispatcher) {
            val wrongUrl = "https://wrong.com"
            var receivedDeepLink: DeepLink? = null

            val job = launch {
                listenForDeepLinks { receivedDeepLink = it }
            }

            Rinku.setDeepLinkMapper(
                deepLinkMapper = FakeDeepLinkMapper(
                    urlToMap = wrongUrl,
                    mapTo = TestUrl
                )
            )
            Rinku.handleDeepLink(
                url = wrongUrl,
                applyMapper = true
            )
            advanceUntilIdle()
            job.cancel()

            assertEquals(
                expected = TestUrl,
                actual = receivedDeepLink?.data,
                message = "Received deep link was not mapped."
            )
        }

    @Test
    fun `on handleDeepLink expects without deeplinkMapper applied - expects throw exception`() =
        runTest(testDispatcher) {
            val wrongUrl = "https://wrong.com"
            var receivedDeepLink: DeepLink? = null

            val job = launch {
                listenForDeepLinks { receivedDeepLink = it }
            }

            try {
                Rinku.handleDeepLink(
                    url = wrongUrl,
                    applyMapper = true
                )
                advanceUntilIdle()
            } catch (e: Exception) {
                assertIs<IllegalStateException>(e)
            }
            job.cancel()
        }


    @Test
    fun `on handleDeepLink expects with deeplinkFilter applied - expects the filter deeplink not be handled`() =
        runTest(testDispatcher) {
            val wrongUrl = "https://wrong.com"
            var receivedDeepLink: DeepLink? = null

            val job = launch {
                listenForDeepLinks { receivedDeepLink = it }
            }

            Rinku.setDeepLinkFilter(
                deepLinkFilter = FakeDeepLinkFilter(
                    invalidUrls = listOf(wrongUrl)
                )
            )

            Rinku.handleDeepLink(
                url = wrongUrl,
                applyFilter = true
            )

            advanceUntilIdle()
            job.cancel()

            assertNull(
                actual = receivedDeepLink,
                message = "Received deep link should be null."
            )
        }

}
