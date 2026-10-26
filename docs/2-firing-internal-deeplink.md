# Firing Internal Deeplink

Rinku simplifies the process of triggering internal deep links directly from your common Kotlin Multiplatform (KMP) code. This feature allows you to easily navigate within your application using a unified approach across all platforms.

#### Triggering a Deep Link
To invoke an internal deep link within your application, pass its URL to `Rinku.handleDeepLink`.
By default, internal calls bypass the configured filter and mapper.

```kotlin
Rinku.handleDeepLink("https://test.deeplink/path?query=true")
```

In this example, https://test.deeplink/path?query=true represents the URL of the deep link. The URL scheme, path, and query parameters should be replaced with values that are relevant to your application's routing structure.

#### Applying a filter or mapper

To apply the filter and mapper registered during platform initialization, enable the
corresponding flags explicitly:

```kotlin
Rinku.handleDeepLink(
    url = "https://test.deeplink/path?query=true",
    applyFilter = true,
    applyMapper = true,
)
```

The filter runs before the mapper. Each flag defaults to `false` and can be enabled
independently. Enabling a flag without configuring the corresponding `DeepLinkFilter`
or `DeepLinkMapper` throws `IllegalStateException` when that step is reached. See
[filters and mappers](4-internal-deeplink-filter.md) for configuration examples.

By leveraging Rinku's handleDeepLink method, you can enhance your application's navigation capabilities, making it easier to programmatically direct users to specific areas of your app from shared KMP code.
