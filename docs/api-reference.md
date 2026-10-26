# Public API reference

This reference describes the source on `main`, which may include unreleased changes.
Check the documentation and sources at the tag matching your Maven dependency before
using a newly added API or platform. Install `dev.theolm:rinku:<version>` and, for
Compose, `dev.theolm:rinku-compose-ext:<version>` at the same version.

Signatures below omit implementation details. See [verified examples](minimal-examples.md)
for executable code and the [setup guide](1-setup.md) for Gradle and platform registration.

## Shared model

Import `dev.theolm.rinku.DeepLink`.

```kotlin
data class DeepLink(
    val data: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)
```

`Clock` is `kotlin.time.Clock`. Construction parses the URL immediately using Ktor;
malformed input can throw. The timestamp is creation time in Unix epoch milliseconds,
not the time the OS received the link. The original string remains in `data`.

Read-only properties:

- `schema: String`: protocol name. The API spelling is **schema**, not `scheme`.
- `host: String`: parsed host.
- `encodedPath: String`: percent-encoded path.
- `pathSegments: List<String>`: decoded path segments, excluding blank segments.
- `encodedQuery: String`: encoded query, without the leading `?`.
- `queryParameterNames: Set<String>`: query keys.
- `parameters: Map<String, String>`: decoded query values. Repeated keys collapse to
  one map entry; the last flattened value wins.

The fragment is retained in `data`, but has no dedicated public property and is not
interpreted as a route. Parsing is not validation or authorization of a route.

## Dispatch and listening

```kotlin
import dev.theolm.rinku.Rinku
import dev.theolm.rinku.listenForDeepLinks
```

```kotlin
object Rinku {
    fun handleDeepLink(url: String): Unit
    fun handleDeepLink(
        url: String,
        applyFilter: Boolean = false,
        applyMapper: Boolean = false,
    ): Unit
}

suspend fun listenForDeepLinks(listener: (DeepLink) -> Unit): Unit
```

These are existing library declarations, not functions to declare in your app.

- Internal dispatch bypasses filters and mappers unless their flags are enabled.
- Filtering happens before mapping. Rejection returns without dispatching.
- An enabled but unconfigured filter/mapper throws `IllegalStateException` when that
  step is reached. Filter and mapper exceptions propagate to the caller.
- Parsing and emission run asynchronously on `Dispatchers.Main`. A successful return
  is not confirmation of parsing or navigation; asynchronous failures are not caught
  by a `try/catch` around `handleDeepLink`.
- Mobile/internal dispatch keeps one pending value, not a FIFO. Rapid dispatches can
  overwrite an unconsumed value. Desktop/Web adapter events instead use a shared FIFO
  of up to 64 pending platform links; overflow rejects the newest event.
- Use one logical listener. Links are consumed once, not broadcast. A pending
  mobile/internal value is consumed before queued platform links.
- `listenForDeepLinks` stays suspended while collecting. Own its coroutine scope and
  cancel it with the UI/application lifecycle. Callbacks run in the collector's
  context; exceptions escape collection. Dispatch to your UI thread as needed.

For Compose, import `dev.theolm.rinku.compose.ext.DeepLinkListener`:

```kotlin
@Composable
fun DeepLinkListener(listener: (DeepLink) -> Unit)
```

This starts collection with `LaunchedEffect(Unit)`, cancels when it leaves composition,
and uses the latest callback without restarting collection on ordinary recomposition.
It does not initialize a platform adapter or navigate automatically.

## Filters and mappers

```kotlin
import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
```

```kotlin
interface DeepLinkFilter {
    fun isValid(url: String): Boolean
}

interface DeepLinkMapper {
    fun map(url: String): String
}
```

Supply these during platform initialization. Filters see the original external URL;
mappers return the URL passed to the shared parser. They do not register URL schemes
or authenticate senders. See [filters and mappers](4-internal-deeplink-filter.md).

## Serializable query parameters

```kotlin
import dev.theolm.rinku.getParameter
import dev.theolm.rinku.models.DeepLinkParam
import kotlinx.serialization.KSerializer
```

```kotlin
fun <T> DeepLink.getParameter(name: String, kSerializer: KSerializer<T>): T

class DeepLinkParam<T>(val name: String, value: T, kSerializer: KSerializer<T>) {
    fun serialize(): String
}

// Additional members of the existing Rinku object:
object Rinku {
    fun buildUrl(url: String, vararg parameters: DeepLinkParam<*>): String
    fun buildUrl(
        schema: String,
        host: String,
        path: String? = null,
        vararg parameters: DeepLinkParam<*>,
    ): String
}
```

Configure the kotlinx.serialization plugin and JSON dependency in your module.
`getParameter` uses the default `Json` configuration, throws `IllegalArgumentException`
for a missing key, and throws a deserialization exception for an incompatible value.
There is no nullable lookup overload or configurable `Json` instance.

`DeepLinkParam.serialize()` returns JSON, **not percent-encoded JSON**. `buildUrl`
concatenates the supplied strings and serialized parameters; it does not validate or
escape them. Use a base URL without a query or fragment. The component overload adds
`/` before a non-null path, so pass `second/third`, not `/second/third`. Percent-encode
query keys and values when constructing externally delivered URLs, especially values
containing `&`, `+`, `#`, or other URL delimiters. See [type-safe parameters](3-type-safe-parameters.md).

## Android

```kotlin
import dev.theolm.rinku.RinkuInit
import dev.theolm.rinku.treatAndFireDeepLink
```

```kotlin
fun ComponentActivity.RinkuInit(
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
)

fun Intent.treatAndFireDeepLink(
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
)
```

`ComponentActivity` is `androidx.activity.ComponentActivity`; `Intent` is
`android.content.Intent`. Call `RinkuInit` once in `onCreate`. It handles the initial
intent and listens for new intents, removing its listener on destruction. The intent
extension processes `dataString` when present, applying filter then mapper. Exceptions
from these steps propagate; final parsing uses asynchronous shared dispatch.

Alternatively, import `dev.theolm.rinku.compose.ext.Rinku` and call the activity extension:

```kotlin
@Composable
fun ComponentActivity.Rinku(
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
    content: @Composable () -> Unit,
)
```

Use it inside the activity's `setContent`, instead of `RinkuInit`. It owns the new-intent
listener for the composition. Keep configuration stable: the listener is installed with
`DisposableEffect(Unit)`. The initial intent is processed in the composable body, so
do not treat recomposition as a guaranteed one-time initialization boundary.

## iOS

Import `dev.theolm.rinku.RinkuIos` in Kotlin, or the exported shared framework in Swift.

```kotlin
class RinkuIos(
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
) {
    fun onDeepLinkReceived(url: String)
    fun onDeepLinkReceived(userActivity: NSUserActivity)
}
```

`NSUserActivity` is `platform.Foundation.NSUserActivity`. Keep an adapter in your
AppDelegate/SceneDelegate and forward the corresponding URL events. The activity
overload accepts only `NSUserActivityTypeBrowsingWeb` activities with a webpage URL.
Both overloads filter, map, and use shared asynchronous dispatch. There is no `start`
or `close` method. See [iOS setup](1-setup.md#ios-setup) for framework export and Swift calls.

## JVM desktop

Import `dev.theolm.rinku.RinkuDesktop`.

```kotlin
class RinkuDesktop(
    applicationId: String,
    schemes: Set<String>,
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
    onError: (Exception) -> Unit = { it.printStackTrace() },
) : AutoCloseable {
    enum class StartResult { PRIMARY, FORWARDED }
    fun start(args: Array<String> = emptyArray()): StartResult
    fun onDeepLinkReceived(url: String)
    override fun close()
}
```

Initialize before creating windows; keep the adapter alive until application shutdown.
Use a stable, nonblank application ID and a nonempty set of valid schemes. Invalid
constructor arguments throw `IllegalArgumentException`. `start` returns `PRIMARY` or
`FORWARDED`; a forwarded process should return without opening a UI. Repeated starts
return the original result. Closing is idempotent; starting after close throws
`IllegalStateException`. Startup/IPC failures throw rather than opening a second instance.

Only syntactically valid absolute URIs matching configured schemes are accepted.
Unsupported/invalid inputs and inputs over 16,384 UTF-16 code units are ignored; more
than 64 accepted startup URLs throws `IllegalArgumentException`. Filtering, mapping,
and queueing run on the receiving thread. Mapping, parsing, overflow, and incoming IPC
errors go to `onError`; keep it non-throwing. Scheme registration and window focus are
application responsibilities. See the [desktop guide](6-desktop.md) for IPC and packaging.

## Web

Import `dev.theolm.rinku.RinkuWeb`.

```kotlin
class RinkuWeb(
    captureInitialUrl: Boolean = true,
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
    onError: (Exception) -> Unit = { it.printStackTrace() },
) {
    fun start()
    fun close()
}
```

Start before creating the UI and close on disposal. Only one adapter can be active per
page. Repeated starts do nothing; closing is idempotent and leaves queued events intact.
A closed adapter cannot restart. Starting after close or while another adapter owns
the page throws `IllegalStateException`. Filter/mapper, parsing, and overflow errors go
to `onError`; keep it non-throwing. Initialization/ownership errors throw to the caller.

The adapter receives the full initial URL (unless disabled) and subsequent `popstate`
events. It does not intercept `pushState`/`replaceState`, observe `hashchange`, write
history, or remove deployment prefixes. Hosting must provide an SPA fallback.

Alternatively, import `dev.theolm.rinku.compose.ext.Rinku`:

```kotlin
@Composable
fun Rinku(
    captureInitialUrl: Boolean = true,
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
    onError: (Exception) -> Unit = { it.printStackTrace() },
    content: @Composable () -> Unit,
)
```

The Web wrapper owns an adapter with `DisposableEffect`. Changing capture/filter/mapper
configuration replaces it and can capture the current URL again; changing `onError`
alone does not. Do not combine it with manual initialization. This is a Web-only
top-level wrapper, not a shared wrapper for iOS or desktop. See the [Web guide](7-web.md).
