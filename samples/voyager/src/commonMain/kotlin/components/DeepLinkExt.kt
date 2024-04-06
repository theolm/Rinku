package components

import cafe.adriel.voyager.core.screen.Screen
import dev.theolm.rinku.DeepLink
import screens.FirstScreen
import screens.FourthScreen
import screens.SecondScreen
import screens.ThirdScreen

fun DeepLink?.toScreenStack() : List<Screen> {
    if (this == null) {
        return listOf(FirstScreen(this))
    }

    val screenPaths = this.pathSegments.mapNotNull {
        when (it) {
            "second" -> SecondScreen()
            "third" -> ThirdScreen()
            "fourth" -> FourthScreen()
            else -> null
        }
    }

    return listOf(FirstScreen(this)) + screenPaths
}