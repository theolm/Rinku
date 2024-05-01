package dev.theolm.rinku.models

/**
 * Interface to filter external deep links.
 * Use case: filter out deep links that are not meant to be handled by the app.
 * Instead of adding a path filter in the androidmanifest or info.plist, you can use
 * this interface to filter out deep links.
 * The application will open, but isValid return false, the deeplink will not be handled by Rinku.
 */
interface DeepLinkFilter {
    fun isValid(url: String): Boolean
}
