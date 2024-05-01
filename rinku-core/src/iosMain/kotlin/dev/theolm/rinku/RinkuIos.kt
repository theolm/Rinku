package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import platform.Foundation.NSUserActivity
import platform.Foundation.NSUserActivityTypeBrowsingWeb
import kotlin.experimental.ExperimentalObjCName


@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "RinkuIos")
class RinkuIos(
    private val deepLinkFilter: DeepLinkFilter? = null,
    private val deepLinkMapper: DeepLinkMapper? = null,
) {
    @ObjCName("onDeepLinkReceived")
    fun onDeepLinkReceived(url: String) {
        treatAndFireDeepLink(url)
    }

    @ObjCName("onDeepLinkReceived")
    fun onDeepLinkReceived(userActivity: NSUserActivity) {
        userActivity.getUrlString()?.let { treatAndFireDeepLink(it) }
    }

    private fun treatAndFireDeepLink(deepLinkUri: String) {
        val shouldFire = deepLinkFilter?.isValid(deepLinkUri) ?: true
        if (shouldFire) {
            val deepLink = deepLinkMapper?.map(deepLinkUri) ?: deepLinkUri
            Rinku.handleDeepLink(deepLink)
        }
    }

    private fun NSUserActivity.getUrlString() =
        if (this.activityType == NSUserActivityTypeBrowsingWeb) {
            this.webpageURL?.absoluteString
        } else {
            null
        }

}
