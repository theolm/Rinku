package dev.theolm.rinku

import platform.Foundation.NSURL
import platform.Foundation.NSUserActivity
import platform.Foundation.NSUserActivityTypeBrowsingWeb


fun onDeepLinkReceived(url: String) {
    filterAndFire(url)
}

fun onDeepLinkReceived(url: NSURL) {
    url.absoluteString?.let { filterAndFire(it) }
}

fun onDeepLinkReceived(userActivity: NSUserActivity) {
    if (userActivity.activityType == NSUserActivityTypeBrowsingWeb) {
        val url = userActivity.webpageURL
        val urlString = url?.absoluteString
        if (urlString != null) {
            filterAndFire(urlString)
        }
    }
}

private fun filterAndFire(deepLinkUri: String) {
    Rinku.handleDeepLink(deepLinkUri)
}