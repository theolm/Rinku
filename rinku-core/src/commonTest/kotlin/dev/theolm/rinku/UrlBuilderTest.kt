package dev.theolm.rinku

import dev.theolm.models.ComplexModel
import dev.theolm.models.Name
import dev.theolm.models.ExpectedMockComplexModel
import dev.theolm.models.ExpectedMockName
import dev.theolm.models.mockComplexModel
import dev.theolm.models.mockName
import dev.theolm.rinku.models.DeepLinkParam
import kotlin.test.Test
import kotlin.test.assertEquals

private const val SCHEMA = "https"
private const val HOST = "dev.theolm"

class UrlBuilderTest {

    @Test
    fun `test BuildUrl without path and parameters`() {
        val expectedUrl = "$SCHEMA://$HOST"
        val actualUrl = UrlBuilder.buildUrl(SCHEMA, HOST)
        assertEquals(
            expected = expectedUrl,
            actual = actualUrl,
            message = "The URL should match the expected format without path and parameters."
        )
    }

    @Test
    fun `test BuildUrl with path and without parameters`() {
        val path = "path/to/resource"
        val expectedUrl = "$SCHEMA://$HOST/$path"
        val actualUrl = UrlBuilder.buildUrl(SCHEMA, HOST, path)
        assertEquals(
            expected = expectedUrl,
            actual = actualUrl,
            message = "The URL should include the path but no parameters."
        )
    }

    @Test
    fun `test BuildUrl with one parameters`() {
        val path = "path"
        val params = arrayOf(
            DeepLinkParam(
                "name",
                mockName,
                Name.serializer()
            ),
        )
        val expectedUrl =
            "$SCHEMA://$HOST/$path?name=$ExpectedMockName"
        val actualUrl = UrlBuilder.buildUrl(SCHEMA, HOST, path, *params)
        assertEquals(expectedUrl, actualUrl, "The URL should include one query parameter.")
    }

    @Test
    fun `test BuildUrl with multiple parameters`() {
        val path = "path"
        val params = arrayOf(
            DeepLinkParam(
                "name",
                mockName,
                Name.serializer()
            ),
            DeepLinkParam(
                "complexModel",
                mockComplexModel,
                ComplexModel.serializer()
            )
        )
        val expectedUrl =
            "$SCHEMA://$HOST/$path?name=$ExpectedMockName&complexModel=$ExpectedMockComplexModel"
        val actualUrl = UrlBuilder.buildUrl(SCHEMA, HOST, path, *params)
        assertEquals(expectedUrl, actualUrl, "The URL should include one query parameter.")
    }

    @Test
    fun `buildUrl with url and parameters`() {
        val url = "$SCHEMA://$HOST/path"
        val params = arrayOf(
            DeepLinkParam(
                "name",
                mockName,
                Name.serializer()
            ),
            DeepLinkParam(
                "complexModel",
                mockComplexModel,
                ComplexModel.serializer()
            )
        )
        val expectedUrl =
            "$SCHEMA://$HOST/path?name=$ExpectedMockName&complexModel=$ExpectedMockComplexModel"
        val actualUrl = UrlBuilder.buildUrl(
            url = url,
            *params
        )
        assertEquals(expectedUrl, actualUrl, "The URL should include one query parameter.")
    }
}
