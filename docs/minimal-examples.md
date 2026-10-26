# Verified integration examples

Start with the [setup guide](1-setup.md) for dependencies and native registration, then
use the [API reference](api-reference.md) for defaults, exceptions, and lifecycle rules.
These examples track `main`, not necessarily the latest published Maven release.

The code blocks below are included directly from compiled/tested source files when
MkDocs builds the site. They are not separately maintained copies. In the raw Markdown,
follow the source links to read the complete code. Missing included files fail the
documentation build.

## Shared parsing and serializable parameters

This complete test class demonstrates parsing a percent-encoded query, deserializing a
model, building an internal URL, and handling a missing parameter. It requires the
kotlinx.serialization compiler plugin and JSON dependency; assertions use `kotlin.test`.
The sample's simple JSON value is safe for internal dispatch; `buildUrl` does not
percent-encode values for external delivery.

[Test source](https://github.com/theolm/Rinku/blob/main/rinku-core/src/commonTest/kotlin/dev/theolm/rinku/DocumentationExamplesTest.kt)

```kotlin
--8<-- "rinku-core/src/commonTest/kotlin/dev/theolm/rinku/DocumentationExamplesTest.kt"
```

Run just these examples on JVM:

```shell
./gradlew :rinku-core:jvmTest --tests 'dev.theolm.rinku.DocumentationExamplesTest'
```

They also belong to the shared test suite run by the existing iOS and JS/Wasm CI jobs.
No platform adapter or OS scheme registration is needed for these parsing tests.

## Shared Compose listener

The Voyager sample keeps a single `DeepLinkListener` in its shared root. Navigation
is application-owned: `toScreenStack()` and `screens.First` belong to the sample, not
Rinku. Use your own route interpretation if you do not use Voyager.

[Shared UI source](https://github.com/theolm/Rinku/blob/main/samples/voyager/shared/src/commonMain/kotlin/App.kt)

```kotlin
--8<-- "samples/voyager/shared/src/commonMain/kotlin/App.kt"
```

For a non-Compose collector, use `listenForDeepLinks` in a lifecycle-owned coroutine
scope; see [common setup](1-setup.md#kmp-only_1). Do not collect simultaneously through
both APIs expecting a broadcast.

## Android entry point

This activity uses the Compose adapter instead of `RinkuInit`. `App` is the shared
root above. Install the sample to exercise native links; intent filters remain required.

[Android source](https://github.com/theolm/Rinku/blob/main/samples/voyager/androidApp/src/main/kotlin/dev/theolm/rinku/MainActivity.kt)

```kotlin
--8<-- "samples/voyager/androidApp/src/main/kotlin/dev/theolm/rinku/MainActivity.kt"
```

```shell
./gradlew :samples:voyager:androidApp:assembleDebug
```

The Android CI job compiles this entry point and runs Maestro deep-link flows.

## iOS entry point

This is the sample's AppDelegate-based entry point, not a SwiftUI `@main` lifecycle.
`ComposeApp` is the exported shared framework; `ContentView` is the sample's SwiftUI
host. Forward URL events from your SceneDelegate instead if your application uses
scenes, including its initial connection events.

[Swift source](https://github.com/theolm/Rinku/blob/main/samples/voyager/iosApp/iosApp/iOSApp.swift)

```swift
--8<-- "samples/voyager/iosApp/iosApp/iOSApp.swift"
```

Open `samples/voyager/iosApp/iosApp.xcodeproj` on macOS and run the `iosApp` scheme on
an Apple Silicon simulator or device. The existing CI tests the Kotlin iOS adapter
and compiles the Compose extension, but **does not build this Swift application**.
Verify Swift integration and cold/warm native URL delivery in Xcode before shipping.

## JVM desktop entry point

Initialize before creating windows, return immediately when forwarding to a primary
instance, and close the adapter after the UI exits. `App` is the shared root above.

[Desktop source](https://github.com/theolm/Rinku/blob/main/samples/voyager/desktopApp/src/jvmMain/kotlin/main.kt)

```kotlin
--8<-- "samples/voyager/desktopApp/src/jvmMain/kotlin/main.kt"
```

```shell
./gradlew :samples:voyager:desktopApp:run --args='rinku-voyager://example/second/third'
```

Desktop CI compiles this sample on macOS, Windows, and Linux. Running through Gradle
does not register a native URL scheme; see [packaging and OS verification](6-desktop.md).

## Web entry point and adapter ownership

The entry point creates the browser UI. `WebDemo` owns the Rinku adapter and implements
application-controlled history buttons. Do not add another adapter around it.

[Web entry source](https://github.com/theolm/Rinku/blob/main/samples/voyager/webApp/src/webMain/kotlin/main.kt)

```kotlin
--8<-- "samples/voyager/webApp/src/webMain/kotlin/main.kt"
```

[Web adapter and history source](https://github.com/theolm/Rinku/blob/main/samples/common/src/webMain/kotlin/dev/theolm/rinku/common/WebDemo.kt)

```kotlin
--8<-- "samples/common/src/webMain/kotlin/dev/theolm/rinku/common/WebDemo.kt"
```

Run one target at a time:

```shell
./gradlew :samples:voyager:webApp:jsBrowserDevelopmentRun
./gradlew :samples:voyager:webApp:wasmJsBrowserDevelopmentRun
```

Web CI bundles both targets and runs shared, browser-history, and Compose lifecycle
tests. Direct navigation/reload also requires an SPA fallback from your production
host. See [Web hosting and deployment prefixes](7-web.md#hosting-and-deployment-prefixes).

## Documentation checks

```shell
python -m pip install mkdocs-material
mkdocs build --strict
```

The documentation-checks CI job builds these pages on pull requests. Executable source
coverage comes from the existing platform CI jobs described above, not from compiling
the API signature summaries in the reference. Native OS registration and packaged
application delivery still require platform-specific manual checks.
