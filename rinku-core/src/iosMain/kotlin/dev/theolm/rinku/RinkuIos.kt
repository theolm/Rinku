package dev.theolm.rinku

import dev.theolm.rinku.models.DeepLinkFilter
import platform.Foundation.NSUserActivity
import platform.Foundation.NSUserActivityTypeBrowsingWeb


fun onDeepLinkReceived(url: String) {
    filterAndFire(url, null)
}

fun onDeepLinkReceived(url: String, deepLinkFilter: DeepLinkFilter) {
    filterAndFire(url, deepLinkFilter)
}

fun onDeepLinkReceived(userActivity: NSUserActivity) {
    userActivity.getUrlString()?.let { filterAndFire(it, null) }
}

fun onDeepLinkReceived(userActivity: NSUserActivity, deepLinkFilter: DeepLinkFilter) {
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
