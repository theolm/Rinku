package dev.theolm.fakes

import dev.theolm.rinku.models.DeepLinkFilter

class FakeDeepLinkFilter(
    private val invalidUrls: List<String>,
) : DeepLinkFilter {
    override fun isValid(url: String): Boolean {
        return !invalidUrls.contains(url)
    }
}