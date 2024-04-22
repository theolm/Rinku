import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.common.models.RandomArgument
import dev.theolm.rinku.getParameter

/**
 * Builds a screen stack based on the deep link.
 * If the deep link is null, the stack will contain only the first screen.
 * If the deep link is not null, the stack will contain the first screen and the screens specified in the deep link.
 */
fun DeepLink?.toScreenStack(): List<Config> {
    if (this == null) {
        return listOf(Config.First())
    }

    //Try to parse RandomArgument from the query parameters
    val parameters = try {
        queryParameterNames.map {
            getParameter(it, RandomArgument.serializer())
        }
    } catch (e: Exception) {
        emptyList<RandomArgument>()
    }

    val screenPaths = this.pathSegments.mapNotNull {
        when (it) {
            "second" -> Config.Second(parameters)
            "third" -> Config.Third(parameters)
            "fourth" -> Config.Fourth(parameters)
            else -> null
        }
    }

    return listOf(Config.First()) + screenPaths
}
