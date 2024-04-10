package dev.theolm.rinku

import com.eygraber.uri.Uri
import kotlinx.datetime.Clock

/**
 * A data class representing a deep link, encapsulating various components of the URI.
 *
 * @property data The raw URI string used to create the deep link object.
 * @property timestamp The timestamp of the deep link creation. Defaults to the current time.
 */
data class DeepLink internal constructor(
    private val data: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    /**
     * The URI object parsed from the raw data string.
     */
    val uri = Uri.parse(data)

    /**
     * The host part of the URI.
     */
    val host: String? get() = uri.host

    /**
     * The path part of the URI. It is decoded.
     */
    val path: String? get() = uri.path

    /**
     * The encoded path part of the URI. It is encoded to ensure special characters are represented correctly.
     */
    val encodedPath: String? get() = uri.encodedPath

    /**
     * A list of path segments, splitting the path by '/'.
     */
    val pathSegments: List<String> get() = uri.pathSegments

    /**
     * The query part of the URI, which is the section after '?'.
     */
    val query: String? get() = uri.query

    /**
     * The encoded query part of the URI, ensuring special characters in the query are correctly encoded.
     */
    val encodedQuery: String? get() = uri.encodedQuery

    /**
     * A set of query parameter names present in the URI.
     */
    val queryParameterNames: Set<String> get() = uri.getQueryParameterNames()

    /**
     * A map of query parameters to their corresponding values. This map is populated from the URI's query parameters.
     */
    val parameters: HashMap<String, String> = HashMap<String, String>().apply {
        queryParameterNames.forEach {
            uri.getQueryParameter(it)?.let { value ->
                put(it, value)
            }
        }
    }
}
