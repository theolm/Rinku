import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import dev.theolm.rinku.common.models.RandomArgument
import kotlinx.serialization.Serializable

/**
 * This class only contains functions related to Decompose
 */
interface AppComponent {
    val stack: Value<ChildStack<*, Screen>>

    fun onBackPress()

    sealed class Screen {
        data class First(val randomArguments: List<RandomArgument> = emptyList()) : Screen()
        data class Second(val randomArguments: List<RandomArgument> = emptyList()) : Screen()
        data class Third(val randomArguments: List<RandomArgument> = emptyList()) : Screen()
        data class Fourth(val randomArguments: List<RandomArgument> = emptyList()) : Screen()
    }
}

class AppComponentImpl(
    private val initialStack: List<Config> = emptyList(),
    componentContext: ComponentContext,
) : AppComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    /**
     * The key aspect of deeplinks in Decompose is to replace the initialStack with the deeplink
     * screen stack
     */
    override val stack: Value<ChildStack<*, AppComponent.Screen>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialStack = {
                initialStack.ifEmpty {
                    listOf(Config.First())
                }
            },
            childFactory = ::child,
        )

    @Suppress("UnusedParameter")
    private fun child(config: Config, componentContext: ComponentContext): AppComponent.Screen =
        when (config) {
            is Config.First -> AppComponent.Screen.First(config.randomArguments)
            is Config.Fourth -> AppComponent.Screen.Fourth(config.randomArguments)
            is Config.Second -> AppComponent.Screen.Second(config.randomArguments)
            is Config.Third -> AppComponent.Screen.Third(config.randomArguments)
        }

    override fun onBackPress() {
        navigation.pop()
    }
}

@Serializable
sealed interface Config {
    @Serializable
    data class First(val randomArguments: List<RandomArgument> = emptyList()) : Config

    @Serializable
    data class Second(val randomArguments: List<RandomArgument> = emptyList()) : Config

    @Serializable
    data class Third(val randomArguments: List<RandomArgument> = emptyList()) : Config

    @Serializable
    data class Fourth(val randomArguments: List<RandomArgument> = emptyList()) : Config
}
