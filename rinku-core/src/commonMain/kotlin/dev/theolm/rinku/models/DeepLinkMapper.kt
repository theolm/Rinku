package dev.theolm.rinku.models

/**
 * Interface to map external deep links into internal deeplinks
 * UseCase: Android and ios application has different deeplinks registered in marketing campaigns.
 * The mapper can be used to map the external deeplink to unique internal deeplink,
 * and the application can handle the deeplink accordingly in a unified way.
 */
interface DeepLinkMapper {
    fun map(url: String): String
}
