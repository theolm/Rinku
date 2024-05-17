package dev.theolm.fakes

import dev.theolm.rinku.models.DeepLinkMapper

class FakeDeepLinkMapper(
    private val urlToMap: String,
    private val mapTo: String,
) : DeepLinkMapper {
    override fun map(url: String): String {
        return if (url == urlToMap) {
            mapTo
        } else {
            url
        }
    }
}