package dev.theolm.rinku.models

interface DeepLinkMapper {
    fun map(url: String): String
}
