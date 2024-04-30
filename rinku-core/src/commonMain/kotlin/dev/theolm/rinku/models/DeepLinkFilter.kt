package dev.theolm.rinku.models

interface DeepLinkFilter {
    fun isValid(url: String): Boolean
}
