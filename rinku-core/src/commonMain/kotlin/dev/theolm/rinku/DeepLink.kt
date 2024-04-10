package dev.theolm.rinku

import io.ktor.http.URLBuilder
import io.ktor.util.flattenEntries
import kotlinx.datetime.Clock

/**
 * A data class representing a deep link, encapsulating various components of the URI.
 *
 * @property data The raw URI string used to create the deep link object.
 * @property timestamp The timestamp of the deep link creation. Defaults to the current time.
 */
data class DeepLink internal constructor(
    internal val data: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    /**
     * The URL object parsed from the raw data string.
     */
    private val url = URLBuilder(data).build()

    /**
     * The schema part of the URI.
     */
    val schema = url.protocol.name

    /**
     * The host part of the URI.
     */
    val host: String = url.host

    /**
     * The encoded path part of the URI. It is encoded to ensure special characters are represented correctly.
     */
    val encodedPath: String = url.encodedPath

    /**
     * A list of path segments, splitting the path by '/'.
     */
    val pathSegments: List<String> = url.pathSegments.filter { it.isNotBlank() }

    /**
     * The encoded query part of the URI, ensuring special characters in the query are correctly encoded.
     */
    val encodedQuery: String = url.encodedQuery

    /**
     * A set of query parameter names present in the URI.
     */
    val queryParameterNames: Set<String> = url.parameters.names()

    /**
     * A map of query parameters to their corresponding values. This map is populated from the URI's query parameters.
     */
    val parameters: Map<String, String> = url.parameters.flattenEntries().toMap()
}
