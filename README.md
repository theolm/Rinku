# Rinku: Deep Link Handling for Kotlin Multiplatform
[![Maven Central](https://img.shields.io/maven-central/v/dev.theolm/rinku)](https://mvnrepository.com/artifact/dev.theolm)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.23-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/theolm/rinku)
[![GitHub License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://opensource.org/licenses/MIT)

Rinku is a lightweight Kotlin Multiplatform library designed to simplify deep link handling across iOS and Android platforms. By abstracting platform-specific details, Rinku enables developers to manage deep links in a unified manner, enhancing code reuse and maintaining consistency across platforms.


## Core Features

- **Unified Deep Link Management:** Architect your deep link logic once, and execute seamlessly across iOS and Android platforms.
- **Simplified Integration:** Designed for rapid setup with minimal configuration requirements, ensuring a smooth start.
- **Universal Compatibility with Navigation Libraries:** Rinku's architecture is designed for compatibility with any navigation library. It delegates the parsing and navigation logic to the application, permitting seamless integration and enhanced flexibility.

## Getting Started

### Prerequisites

- Kotlin Multiplatform project setup
- For Android: Minimum SDK version 22
- For iOS: iOS 13.0 or later

### Installation Process

#### Gradle Configuration

In your `build.gradle.kts` file you need to:
- Include Rinku in commonMain as an api (this is required to export it to iOS)
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
        }
    }
}
```

#### Initial Setup
This guide presupposes the prior configuration of deeplinks within the native platforms:

- [Android deeplink](https://developer.android.com/training/app-links/deep-linking?hl=pt-br)
- [iOS URL scheme](https://developer.apple.com/documentation/xcode/defining-a-custom-url-scheme-for-your-app)
- [iOS universal link](https://developer.apple.com/documentation/xcode/supporting-universal-links-in-your-app)

### Android setup

On the Android app inside the `setContent` use `ComponentActivity.Riku` extension to wrap the root of your app.

```kt
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
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        Rinku.shared.handleDeepLink(url: url.absoluteString)
        return true
    }
    
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        if userActivity.activityType == NSUserActivityTypeBrowsingWeb, let url = userActivity.webpageURL {
            let urlString = url.absoluteString
            Rinku.shared.handleDeepLink(url: urlString)
        }
        return true
    }
    ...
}
```

### Common setup
In the common code you just need to listen to the deeplinks and treat them as you need. Once the application (Android or iOS) recieves a deeplink it will parse it into a `Deeplink` data class and pass it into the `DeepLinkListener`.

Example RootApp in commonMain:
```kt
@Composable
fun RootApp() {
    var deepLink by remember { mutableStateOf<DeepLink?>(null) }
    DeepLinkListener { deepLink = it }
    MainScreen(deepLink)
}
```

## Demonstrative Samples
TThe library includes two illustrative examples utilizing the foremost multiplatform navigation libraries: [Voyager](https://voyager.adriel.cafe/) and [Decompose](https://github.com/arkivanov/Decompose)

- [Voyager sample](https://github.com/theolm/Rinku/tree/main/samples/voyager)
- [Decompose sample](https://github.com/theolm/Rinku/tree/main/samples/decompose)

*Note: Only the Voyager sample includes an iOS application. Nonetheless, the setup for Decompose would mirror that of Voyager.*

## License
Rinku is released under the MIT License. See the LICENSE file for more details.