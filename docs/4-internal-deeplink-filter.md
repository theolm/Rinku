# Deep Link Filters and Mappers

Rinku can reject unwanted external deep links after the platform delivers them to your
application. Implement `DeepLinkFilter` and pass it to the platform initializer. A URL
is processed only when `isValid` returns `true`.

The filter does **not** replace URL scheme registration, Android intent filters, or iOS
URL scheme and Universal Link configuration. Those settings determine whether the OS
delivers the URL to your app. A Rinku filter only controls subsequent processing; it
does not authenticate the sender or replace authorization checks.

```kotlin
import dev.theolm.rinku.models.DeepLinkFilter

object ExampleFilter : DeepLinkFilter {
    override fun isValid(url: String): Boolean =
        url == "rinku://dev.theolm/screenC/"
}
```

This example deliberately accepts one exact URL. Adapt validation to your supported
schemes, hosts, paths, and parameters.

### Deep link mapper

A `DeepLinkMapper` transforms an accepted external URL into the internal URL your
navigation expects. For example:

- Android and iOS marketing links can map to one shared internal route.
- A link to screen C can expand into the full navigation stack A → B → C.

```kotlin
import dev.theolm.rinku.models.DeepLinkMapper

object ExampleMapper : DeepLinkMapper {
    override fun map(url: String): String {
        return if (url == "rinku://dev.theolm/screenC/") {
            "rinku://dev.theolm/screenA/screenB/screenC/"
        } else {
            url
        }
    }
}
```

### Initialization

On Android, pass the objects to `RinkuInit` inside `ComponentActivity.onCreate`:

```kotlin
RinkuInit(
    deepLinkFilter = ExampleFilter,
    deepLinkMapper = ExampleMapper,
)
```

Alternatively, use the Compose wrapper inside the activity's `setContent`:

```kotlin
Rinku(deepLinkFilter = ExampleFilter, deepLinkMapper = ExampleMapper) {
    App()
}
```

Use only one initialization approach. `RinkuIos`, `RinkuDesktop`, and `RinkuWeb` also
accept `deepLinkFilter` and `deepLinkMapper`; see the [setup](1-setup.md),
[desktop](6-desktop.md), and [Web](7-web.md) guides.

External links are filtered **before** mapping, so validate the original external URL.
Internal calls to `Rinku.handleDeepLink(url)` bypass both steps unless you explicitly
enable `applyFilter` and/or `applyMapper`; see [internal deep links](2-firing-internal-deeplink.md).
