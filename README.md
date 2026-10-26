# Rinku: Deep Link Handling for Kotlin Multiplatform
[![Maven Central](https://img.shields.io/maven-central/v/dev.theolm/rinku)](https://central.sonatype.com/artifact/dev.theolm/rinku)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.21-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/theolm/rinku)
[![GitHub License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://opensource.org/licenses/MIT)

Rinku is a lightweight Kotlin Multiplatform library designed to simplify deep link handling across iOS, Android, JVM desktop (macOS, Windows, and Linux), and Web (Kotlin/JS and Kotlin/Wasm browsers). By abstracting platform-specific details, Rinku enables developers to manage deep links in a unified manner, enhancing code reuse and maintaining consistency across platforms.


## Core Features

- **Unified Deep Link Management:** Architect your deep link logic once, and execute across iOS, Android, JVM desktop, and Web platforms.
- **Simplified Integration:** Designed for rapid setup with minimal configuration requirements, ensuring a smooth start.
- **Universal Compatibility with Navigation Libraries:** Rinku's architecture is designed for compatibility with any navigation library (Voyager, Decompose, custom solutions, etc.). It delegates the parsing and navigation logic to the application, permitting seamless integration and enhanced flexibility.

## Prerequisites

- Kotlin Multiplatform project setup
- For Android: Minimum SDK version 23
- For iOS: iOS 13.0 or later
- Supported iOS architectures: `iosArm64` and `iosSimulatorArm64`. Intel Mac simulators (`iosX64`) are no longer supported by either module.
- For desktop: JVM 17 or later. See the [desktop setup guide](docs/6-desktop.md) for URL schemes, single-instance delivery, and packaging.
- For Web: Kotlin/JS or Kotlin/Wasm (`wasmJs`) in a browser. See the [Web setup guide](docs/7-web.md) for initial URLs, browser history, Compose integration, and hosting.

The library is available via Maven Central
```kt
implementation("dev.theolm:rinku:<latest_version>")
implementation("dev.theolm:rinku-compose-ext:<latest_version>")
```

> For iOS, Swift export, filters, mappers, and other platforms, see the [full documentation](https://theolm.github.io/Rinku/1-setup/).

## Quick Start

### 1. Initialize on Android

Inside `MainActivity.onCreate`, call `RinkuInit()`:

```kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RinkuInit()
        setContent { App() }
    }
}
```

If you use Compose Multiplatform, wrap your root instead:

```kt
setContent {
    Rinku { App() }
}
```

### 2. Listen and navigate in common code

```kt
@Composable
fun RootApp() {
    var deepLink by remember { mutableStateOf<DeepLink?>(null) }
    DeepLinkListener { deepLink = it }
    MainScreen(deepLink)
}
```

### 3. Trigger a deep link

```kt
Rinku.handleDeepLink("https://example.com/second/third?user={\"name\":\"Theo\"}")

// Access parsed components
deepLink?.host
deepLink?.pathSegments       // ["second", "third"]
deepLink?.parameters         // { "user" = "{\"name\":\"Theo\"}" }
```

For type-safe arguments, custom URL schemes, filters, and iOS setup, see the [full documentation](https://theolm.github.io/Rinku/).

## Samples

The `samples/` directory contains runnable examples demonstrating integration with popular navigation libraries:

- `samples/common` — Shared UI components and screen definitions used by both samples
- `samples/voyager` — [Voyager](https://voyager.adriel.cafe/) integration (shared module, Android app, iOS app, desktop app, JS/Wasm Web app)
- `samples/decompose` — [Decompose](https://github.com/arkivanov/Decompose) integration (shared module, Android app, desktop app, JS/Wasm Web app)

## Documentation
You can find the full documentation [here](https://theolm.github.io/Rinku/)

- [Public API reference](docs/api-reference.md): imports, signatures, defaults, exceptions, and lifecycle contracts.
- [Verified integration examples](docs/minimal-examples.md): source-backed examples and platform verification commands.

For coding agents, [llms.txt](https://theolm.github.io/Rinku/llms.txt) provides a concise overview and links to the Markdown documentation.


## License
Rinku is released under the MIT License. See the LICENSE file for more details.
