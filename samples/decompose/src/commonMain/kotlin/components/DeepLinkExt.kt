package components


import Config
import dev.theolm.rinku.DeepLink

/**
 * Builds a screen stack based on the deep link.
 * If the deep link is null, the stack will contain only the first screen.
 * If the deep link is not null, the stack will contain the first screen and the screens specified in the deep link.
 */
fun DeepLink?.toScreenStack() : List<Config> {
    if (this == null) {
        return listOf(Config.First)
    }

    val screenPaths = this.pathSegments.mapNotNull {
        when (it) {
            "second" -> Config.Second
            "third" -> Config.Third
            "fourth" -> Config.Fourth
            else -> null
        }
    }

    return listOf(Config.First) + screenPaths
}