# Setup

This guide presupposes the prior configuration of deeplinks within the native platforms:

- [Android deeplink](https://developer.android.com/training/app-links/deep-linking?hl=pt-br)
- [iOS URL scheme](https://developer.apple.com/documentation/xcode/defining-a-custom-url-scheme-for-your-app)
- [iOS universal link](https://developer.apple.com/documentation/xcode/supporting-universal-links-in-your-app)
- [Desktop URL schemes and single-instance setup](6-desktop.md)
- [Web browser history and SPA hosting](7-web.md)

### Installation Process
The library is available via Maven Central
```kt
implementation("dev.theolm:rinku:<latest_version>")
implementation("dev.theolm:rinku-compose-ext:<latest_version>")
```

#### Gradle Configuration

Both Rinku modules support `iosArm64` (physical devices) and `iosSimulatorArm64` (Apple Silicon simulators). The `iosX64` target (Intel Mac simulators) is no longer supported.

In your `build.gradle.kts` file you need to:
- Include Rinku in commonMain as an api (this is required to export it to iOS)
- If you are using Compose multiplatform, also include the compose-extensions
- Export the lib in the ios framework


Example:
```kt
kotlin {
    ...
    listOf(
        iosArm64(),
        iosSimulatorArm64()
        // Specify iOS targets
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            // Export of Rinku library to the iOS framework
            export("dev.theolm:rinku:<latest_version>")
            ...
        }
    }
    ...
    sourceSets {
        commonMain.dependencies {
            api("dev.theolm:rinku:<latest_version>")

            // For compose multiplatform projects
            implementation("dev.theolm:rinku-compose-ext:<latest_version>")
        }
    }
}
```

### Android setup
The library provides two types of initialization (KMP only and Compose). Use the one that fits your needs.

#### KMP only
On the Android app, inside the `onCreate`, call the extension `RinkuInit()`.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RinkuInit()
        setContent {
            App()
        }
    }
}
```

#### With Compose multiplatform
First, make sure you included `rinku-compose-ext` in your `commonMain` dependencies.
In the Android app, inside `setContent`, use the `ComponentActivity.Rinku` extension to wrap the root of your app.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Rinku {
                App()
            }
        }
    }
}
```

### iOS setup

For the iOS platform, deep links are processed within the AppDelegate or SceneDelegate, contingent on the project's configuration. The primary objective is to intercept the platform-specific deep link and relay it as an argument to Rinku's handleDeepLink(url) method.

Example within AppDelegate:

```swift
@UIApplicationMain
class AppDelegate: NSObject, UIApplicationDelegate {
    ...
    // Provide deepLinkFilter and deepLinkMapper if needed
    let rinku = RinkuIos.init(deepLinkFilter: nil, deepLinkMapper: nil)
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        rinku.onDeepLinkReceived(url: url.absoluteString)
        return true
    }
    
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        if userActivity.activityType == NSUserActivityTypeBrowsingWeb, let url = userActivity.webpageURL {
            let urlString = url.absoluteString
            rinku.onDeepLinkReceived(userActivity: userActivity)
        }
        return true
    }
    ...
}
```

### Web setup

Use `RinkuWeb.start()` in your browser entry point or the Web Compose `Rinku { App() }`
wrapper. Both JS and Wasm receive the initial URL and browser Back/Forward through the
same common listeners. See the [Web guide](7-web.md) for lifecycle, history ownership,
filters, mappers, samples, and hosting configuration.

### Common setup
In common code, listen for deep links and handle them as your application requires.
Android, iOS, desktop, and Web all deliver the same `DeepLink` model through these listeners.

#### Using Compose

Example RootApp in commonMain:

```kotlin
@Composable
fun RootApp() {
    var deepLink by remember { mutableStateOf<DeepLink?>(null) }
    DeepLinkListener { deepLink = it }
    MainScreen(deepLink)
}
```

#### KMP only
Just use the suspend function `listenForDeepLinks` and react as you will.

Example inside a Decompose component (navigation model, interface members, and the
application-specific `toScreenStack()` extension are omitted). Create a coroutine scope
for the component and cancel it when the component is destroyed:

```kotlin
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppComponentImpl(
    private val initialStack: List<Config> = emptyList(),
    componentContext: ComponentContext,
) : AppComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch { initDeepLinkListener() }
    }

    private suspend fun initDeepLinkListener() {
        listenForDeepLinks {
            navigation.replaceAll(
                *it.toScreenStack().toTypedArray()
            )
        }
    }
}

```
