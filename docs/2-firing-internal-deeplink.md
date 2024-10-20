# Firing Internal Deeplink

To invoke an internal deep link within your application, use the handleDeepLink method provided by Rinku. This method accepts a single parameter: the URL of the deep link you wish to trigger.

```kotlin
Rinku.handleDeepLink("https://test.deeplink/path?query=true")
```

In this example, https://test.deeplink/path?query=true represents the URL of the deep link. The URL scheme, path, and query parameters should be replaced with values that are relevant to your application's routing structure.

By leveraging Rinku's handleDeepLink method, you can enhance your application's navigation capabilities, making it easier to programmatically direct users to specific areas of your app from shared KMP code.