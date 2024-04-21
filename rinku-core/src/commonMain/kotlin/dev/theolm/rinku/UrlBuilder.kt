package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkParam

internal object UrlBuilder {
    fun buildUrl(schema: String, host: String, path: String? = null, vararg parameters: DeepLinkParam<*>): String {
        val urlBuilder = StringBuilder("$schema://$host")
        if (path != null) {
            urlBuilder.append("/$path")
        }
        return buildUrl(urlBuilder.toString(), *parameters)
    }

    fun buildUrl(url: String, vararg parameters: DeepLinkParam<*>): String {
        val urlBuilder = StringBuilder(url)
        if (parameters.isNotEmpty()) {
            urlBuilder.append("?")
            urlBuilder.append(parameters.joinToString("&") { "${it.name}=${it.serialize()}" })
        }
        return urlBuilder.toString()
    }
}