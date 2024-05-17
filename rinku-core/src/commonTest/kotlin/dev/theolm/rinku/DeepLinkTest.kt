package dev.theolm.rinku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val Schema = "https://"
private const val Host = "dev.theolm"

class DeepLinkTest {

    @Test
    fun `test host parsing`() {
        val url = "${Schema}$Host/path?query=value"
        val deepLink = DeepLink(url)
        assertEquals(
            expected = Host,
            actual = deepLink.host,
            message = "Host should be parsed correctly."
        )
    }

    @Test
    fun `test path parsing`() {
        val url = "${Schema}$Host/path/to/resource"
        val deepLink = DeepLink(url)
        assertEquals(
            expected = "/path/to/resource",
            actual = deepLink.encodedPath,
            message = "Path should be parsed correctly."
        )
    }

    @Test
    fun `test query parsing`() {
        val url = "${Schema}$Host/path?query1=value1&query2=value2"
        val deepLink = DeepLink(url)
        assertEquals(
            expected = "query1=value1&query2=value2",
            actual = deepLink.encodedQuery,
            message = "Encoded query should be parsed correctly."
        )
    }

    @Test
    fun `test query parameter names parsing`() {
        val url = "${Schema}$Host/path?query1=value1&query2=value2"
        val deepLink = DeepLink(url)
        val expectedParams = setOf("query1", "query2")
        assertTrue(
            actual = expectedParams.all { it in deepLink.queryParameterNames },
            message = "Query parameter names should be parsed correctly."
        )
    }

    @Test
    fun `test parameters parsing`() {
        val url = "${Schema}$Host/path?query1=value1&query2=value2"
        val deepLink = DeepLink(url)
        val expectedParamMap = hashMapOf("query1" to "value1", "query2" to "value2")
        assertEquals(
            expected = expectedParamMap,
            actual = deepLink.parameters,
            message = "Parameters should be parsed correctly."
        )
    }

    @Test
    fun `test path with encoded values`() {
        val url = "${Schema}$Host/path%20with%20spaces"
        val deepLink = DeepLink(url)
        assertEquals(
            expected = "/path%20with%20spaces",
            actual = deepLink.encodedPath,
            message = "Path should be decoded correctly."
        )
    }

    @Test
    fun `test query with encoded values`() {
        val url = "${Schema}$Host/path?query%20name=value%20with%20spaces"
        val deepLink = DeepLink(url)
        assertEquals(
            expected = "query%20name=value%20with%20spaces",
            actual = deepLink.encodedQuery,
            message = "Query should be decoded correctly."
        )
    }

    @Test
    fun `test parameter decoding with encoded values`() {
        val url = "${Schema}$Host/path?query%20name=value%20with%20spaces"
        val deepLink = DeepLink(url)
        val expectedParamMap = hashMapOf("query name" to "value with spaces")
        assertEquals(
            expected = expectedParamMap,
            actual = deepLink.parameters,
            message = "Parameters should be decoded correctly."
        )
    }
}
