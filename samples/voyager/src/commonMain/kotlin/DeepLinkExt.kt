import cafe.adriel.voyager.core.screen.Screen
import dev.theolm.rinku.DeepLink
import screens.First
import screens.Fourth
import screens.Second
import screens.Third

fun DeepLink?.toScreenStack(): List<Screen> {
    if (this == null) {
        return listOf(First(this))
    }

    val screenPaths = this.pathSegments.mapNotNull {
        when (it) {
            "second" -> Second()
            "third" -> Third()
            "fourth" -> Fourth()
            else -> null
        }
    }

    return listOf(First(this)) + screenPaths
}
