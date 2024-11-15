# Setup

This guide presupposes the prior configuration of deeplinks within the native platforms:

- [Android deeplink](https://developer.android.com/training/app-links/deep-linking?hl=pt-br)
- [iOS URL scheme](https://developer.apple.com/documentation/xcode/defining-a-custom-url-scheme-for-your-app)
- [iOS universal link](https://developer.apple.com/documentation/xcode/supporting-universal-links-in-your-app)

### Installation Process
The library is available via Maven Central
```kt
implementation("dev.theolm:rinku:<latest_version>")
implementation("dev.theolm:rinku-compose-ext:<latest_version>")
```

#### Gradle Configuration

In your `build.gradle.kts` file you need to:
- Include Rinku in commonMain as an api (this is required to export it to iOS)
- If you are using Compose multiplatform, also include the compose-extensions
- Export the lib in the ios framework


Example:
```kt
kotlin {
    ...
    listOf(
        iosX64(),
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
The library provides two types of initialization (KMP only and Compose), you should use the one that fit your needs.

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
First make sure you included the `rinku-compose-ext`in your `commonMain`.
On the Android app inside the `setContent` use `ComponentActivity.Riku` extension to wrap the root of your app.

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

### Common setup
In the common code you just need to listen to the deeplinks and treat them as you need. Once the application (Android or iOS) recieves a deeplink it will parse it into a `Deeplink` data class and pass it into the listener. Use the listener that suite your project.

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

Example inside a Decompose component:

```kotlin
class AppComponentImpl(
    private val initialStack: List<Config> = emptyList(),
    componentContext: ComponentContext,
) : AppComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    init {
        launch { initDeepLinkListener() }
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