package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkParam
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DocumentationExamplesTest {
    @Serializable
    data class User(val name: String)

    @Test
    fun parseEncodedLink() {
        val link = DeepLink(
            "https://example.com/second/third?user=%7B%22name%22%3A%22Theo%22%7D"
        )

        assertEquals("https", link.schema)
        assertEquals("example.com", link.host)
        assertEquals(listOf("second", "third"), link.pathSegments)
        assertEquals(User("Theo"), link.getParameter("user", User.serializer()))
    }

    @Test
    fun buildInternalLink() {
        val parameter = DeepLinkParam("user", User("Theo"), User.serializer())
        val url = Rinku.buildUrl("https://example.com/second", parameter)

        assertEquals("{\"name\":\"Theo\"}", parameter.serialize())
        assertEquals(User("Theo"), DeepLink(url).getParameter("user", User.serializer()))
    }

    @Test
    fun buildLinkFromComponents() {
        val url = Rinku.buildUrl(schema = "https", host = "example.com", path = "second/third")

        assertEquals("https://example.com/second/third", url)
    }

    @Test
    fun rejectMissingParameter() {
        val link = DeepLink("https://example.com/second")

        assertFailsWith<IllegalArgumentException> {
            link.getParameter("user", User.serializer())
        }
    }
}
