package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkFilter
import platform.Foundation.NSUserActivity
import platform.Foundation.NSUserActivityTypeBrowsingWeb
import kotlin.experimental.ExperimentalObjCName


@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "RinkuIos")
class RinkuIos(
    private val deepLinkFilter: DeepLinkFilter? = null,
) {
    @ObjCName("onDeepLinkReceived")
    fun onDeepLinkReceived(url: String) {
        filterAndFire(url, deepLinkFilter)
    }

    @ObjCName("onDeepLinkReceived")
    fun onDeepLinkReceived(userActivity: NSUserActivity) {
        userActivity.getUrlString()?.let { filterAndFire(it, deepLinkFilter) }
    }

    private fun filterAndFire(deepLinkUri: String, deepLinkFilter: DeepLinkFilter?) {
        val shouldFire = deepLinkFilter?.isValid(deepLinkUri) ?: true
        if (shouldFire) {
            Rinku.handleDeepLink(deepLinkUri)
        }
    }

    private fun NSUserActivity.getUrlString() =
        if (this.activityType == NSUserActivityTypeBrowsingWeb) {
            this.webpageURL?.absoluteString
        } else {
            null
        }
}

