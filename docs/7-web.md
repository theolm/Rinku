# Web (Kotlin/JS and Kotlin/Wasm)

Both `rinku-core` and `rinku-compose-ext` support `js { browser() }` and `wasmJs { browser() }`.
Use the same Maven coordinates and shared `DeepLink`, filters, mappers, and listeners as on
Android, iOS, and desktop. Rinku is a deep-link receiver, not a Web router.

## Browser contract

`RinkuWeb` receives:

- The full current `window.location.href` at startup, including the site root, unless
  `captureInitialUrl = false`.
- The current full URL on each `popstate` event, such as browser Back and Forward.

Rinku does **not** write browser history, intercept `pushState` or `replaceState`, observe
`hashchange`, or convert a fragment into a route. Path routing is supported; hash routing
is outside this adapter's contract. A fragment in a received URL is preserved in
`DeepLink.data`, not interpreted as path segments.

Filters run before mappers, once per browser event. The mapper's output goes through the
existing common parser and bounded FIFO queue. Up to 64 platform links can wait for a
listener; overflow rejects the newest link and calls `onError`. Distinct browser events
with identical URLs are not deduplicated.

## Manual initialization

In your Web entry point, start the adapter before creating the UI:

```kotlin
import dev.theolm.rinku.RinkuWeb

val rinku = RinkuWeb(
    captureInitialUrl = true,
    deepLinkFilter = myFilter, // optional
    deepLinkMapper = myMapper, // optional
    onError = { error -> println("Deep-link error: $error") },
)
rinku.start()
// Create your UI. Call rinku.close() when disposing the application.
```

Use only one active adapter per page. Repeated `start()` calls do nothing; they do not
replay the initial URL. `close()` is idempotent, removes the browser listener, and leaves
already queued links available to shared listeners. A closed adapter cannot restart;
create a new one instead. Starting another adapter while one is active throws.

Filter/mapper, parsing, and overflow failures go to `onError`. Initialization/ownership
errors throw to the caller. Error callbacks should not throw. The default callback prints
the exception. Filters and mappers supplied at startup are also registered for explicit
`Rinku.handleDeepLink(..., applyFilter = true, applyMapper = true)` calls, as on desktop.

## Compose initialization

Instead of initializing manually, wrap the root of your Web composition:

```kotlin
import dev.theolm.rinku.compose.ext.Rinku

Rinku(
    captureInitialUrl = true,
    deepLinkFilter = myFilter,
    deepLinkMapper = myMapper,
) {
    App()
}
```

The wrapper uses `DisposableEffect` to start/close its adapter. Ordinary recomposition
does not reinstall it or replay startup. Changing capture/filter/mapper configuration
replaces the adapter, capturing the initial URL again if enabled. Remember filter/mapper
objects rather than constructing new instances on every recomposition. Updating `onError`
alone does not restart the adapter.

Do not combine the wrapper with a manually started adapter.

## Shared navigation remains unchanged

Use one logical listener, either the suspending common API:

```kotlin
listenForDeepLinks { deepLink ->
    // Application-specific parsing/navigation, just as on the other platforms.
}
```

or the existing Compose listener inside `App()`:

```kotlin
DeepLinkListener { deepLink ->
    // Update Voyager, Decompose, or your own navigation.
}
```

Events are consumed once, not broadcast. Initial URLs received before the listener starts
remain queued. The adapter never depends on a navigation library.

## Application-owned navigation and history

`pushState` and `replaceState` do not emit `popstate`. When navigating internally, the
application must update its router itself, or explicitly notify Rinku **once**:

```kotlin
window.history.pushState(null, "", "/second/third?source=web")
Rinku.handleDeepLink(window.location.href, applyFilter = true, applyMapper = true)
```

Enable the flags only if you configured the respective filter/mapper. Do not both navigate
directly and dispatch the same link if that would duplicate the transition. Do not write a
new history entry from a Back/Forward callback: doing so creates a navigation loop. Do not
install a second browser-to-router integration for the same events without coordinating it.

## Hosting and deployment prefixes

Rinku preserves the URL, including deployment prefixes. For
`https://example.com/app/second?source=web`, the shared path contains `app` and `second`.
If navigation should see only `second`, remove the prefix in your `DeepLinkMapper`,
preserving the origin, encoded path, query, and fragment. There is no adapter-level
`basePath` setting.

The host must serve your SPA entry point for application routes such as `/second/third`,
including direct requests and reloads. Static resources must still resolve from the
deployment root. For example, configure an SPA rewrite/fallback on your host, and use
`<base href="/app/">` when deploying under `/app/`. Rinku cannot fix server-side 404s.

## Samples

Both navigation samples include a `webApp` module. Run one command at a time:

```shell
./gradlew :samples:voyager:webApp:jsBrowserDevelopmentRun
./gradlew :samples:voyager:webApp:wasmJsBrowserDevelopmentRun
./gradlew :samples:decompose:webApp:jsBrowserDevelopmentRun
./gradlew :samples:decompose:webApp:wasmJsBrowserDevelopmentRun
```

Each sample has application-owned history controls. Open `/second/third?source=web`
directly, navigate with the toolbar, then try browser Back, Forward, and reload. The
development servers include an SPA fallback. Existing in-screen back controls operate on
the navigation library's stack; the toolbar's browser controls operate on browser history.

For a prefixed deployment, change both `<base href>` and the `rinku-base-path` meta tag in
the sample's `src/webMain/resources/index.html` to the same slash-terminated prefix,
such as `/app/`. The sample mapper removes that prefix before shared navigation. Its
filter accepts same-origin URLs.

Build distributable bundles with `jsBrowserDistribution` or `wasmJsBrowserDistribution`
on either `webApp` module. Wasm requires a browser with Wasm GC support; Kotlin/JS is a
separate build, not an automatic runtime fallback.

## Tests

Install Chrome/Chromium and set `CHROME_BIN` if it is not discoverable. Run:

```shell
./gradlew :rinku-core:jsBrowserTest :rinku-core:wasmJsBrowserTest \
  :rinku-compose-ext:jsBrowserTest :rinku-compose-ext:wasmJsBrowserTest
```

The suites run shared tests plus real browser-history tests and Compose runtime
lifecycle tests. CI also bundles both samples in JS and Wasm. Linking Compose Web builds
can require more than the project's default 2 GB compiler heap; CI uses
`--max-workers=1 -Pkotlin.daemon.jvmargs=-Xmx4g` to serialize compilation with a larger heap.
Use those options locally when linking the samples or Compose tests if needed.
Yarn lockfiles are tracked;
after intentionally changing npm dependencies, regenerate them with
`./gradlew kotlinUpgradeYarnLock kotlinWasmUpgradeYarnLock`.
