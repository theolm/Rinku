import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

interface AppComponent {
    val stack: Value<ChildStack<*, Screen>>

    fun onBackPress()

    sealed class Screen {
        data object First : Screen()
        data object Second : Screen()
        data object Third : Screen()
        data object Fourth : Screen()
    }
}

class AppComponentImpl(
    private val initialStack: List<Config> = emptyList(),
    componentContext: ComponentContext,
) : AppComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, AppComponent.Screen>> =
        childStack(
            source = navigation,
            initialStack = {
                initialStack.ifEmpty {
                    listOf(Config.First)
                }
            },
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): AppComponent.Screen =
        when (config) {
            Config.First -> AppComponent.Screen.First
            Config.Fourth -> AppComponent.Screen.Fourth
            Config.Second -> AppComponent.Screen.Second
            Config.Third -> AppComponent.Screen.Third
        }

    override fun onBackPress() {
        navigation.pop()
    }
}

@Parcelize // kotlin-parcelize plugin must be applied if you are targeting Android
sealed interface Config : Parcelable {
    data object First : Config
    data object Second : Config
    data object Third : Config
    data object Fourth : Config
}