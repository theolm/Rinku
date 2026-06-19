import cafe.adriel.voyager.core.screen.Screen
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.common.models.RandomArgument
import dev.theolm.rinku.getParameter
import screens.First
import screens.Fourth
import screens.Second
import screens.Third

@Suppress("TooGenericExceptionCaught", "SwallowedException")
fun DeepLink?.toScreenStack(): List<Screen> {
    if (this == null) {
        return listOf(First(this))
    }

    val parameters = try {
        queryParameterNames.map {
            getParameter(queryParameterNames.first(), RandomArgument.serializer())
        }
    } catch (e: Exception) {
        emptyList()
    }

    val screenPaths = this.pathSegments.mapNotNull {
        when (it) {
            "second" -> Second(parameters)
            "third" -> Third(parameters)
            "fourth" -> Fourth(parameters)
            else -> null
        }
    }

    return listOf(First(this)) + screenPaths
}
