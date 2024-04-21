package dev.theolm.rinku

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val Schema = "https://"
private const val Host = "dev.theolm"
class DeepLinkExtTest {

    @Serializable
    private data class Name(val name: String)

    @Serializable
    private data class WrongSerializer(val error: String)

    @Test
    fun `when getParameter - expect to return a tapesafe parameter`() {
        val url = "${Schema}$Host/path?test={\"name\": \"Theo\"}"
        val deepLink = DeepLink(url)

        val test = deepLink.getParameter(name = "test", kSerializer = Name.serializer())

        assertEquals(
            expected = "Theo",
            actual = test.name,
        )
    }

    @Test
    fun `when getParameter with the wrong name - expect to throw an exception when parameter not found`() {
        val url = "${Schema}$Host/path?test={\"name\": \"Theo\"}"
        val deepLink = DeepLink(url)

        try {
            deepLink.getParameter(name = "wrong name", kSerializer = Name.serializer())
        } catch (e: IllegalArgumentException) {
            assertEquals(
                expected = "Parameter wrong name not found.",
                actual = e.message,
            )
        }
    }

    @Test
    fun `when getParameter passing the wrong serializer - expect to throw an exception`() {
        val url = "${Schema}$Host/path?test={\"name\": \"Theo\"}"
        val deepLink = DeepLink(url)

        try {
            deepLink.getParameter(name = "test", kSerializer = WrongSerializer.serializer())
        } catch (e: Exception) {
            assertTrue(e.message.orEmpty().contains("Unexpected JSON"))
        }
    }
}
