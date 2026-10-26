# Type-safe parameters

Rinku supports reading and creating type-safe parameters using [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization). Configure the serialization compiler plugin and JSON dependency in your app/module before using the following functions.

### Getting parameters
Use the `DeepLink.getParameter` extension with the query parameter name and the `KSerializer` of the corresponding Kotlin class. A missing parameter throws `IllegalArgumentException`; an invalid value throws a deserialization exception.

Example:

```kotlin
import dev.theolm.rinku.DeepLink
import dev.theolm.rinku.getParameter
import kotlinx.serialization.Serializable

@Serializable
data class Name(val name: String)

fun example() {
    val url = "https://example.com/dev/path?test=%7B%22name%22%3A%22Theo%22%7D"
    val deepLink = DeepLink(url)

    val param: Name = deepLink.getParameter(name = "test", kSerializer = Name.serializer())
}

```

The query value is the percent-encoded JSON `{"name":"Theo"}`. Rinku decodes the query
parameter before deserializing it.

### Build a URL using Serializable classes
Rinku also provides the `Rinku.buildUrl` helper to build internal deep links with serializable parameters. Build the URL, then pass it to `Rinku.handleDeepLink`.

Example:

```kotlin
import dev.theolm.rinku.Rinku
import dev.theolm.rinku.models.DeepLinkParam
import kotlinx.serialization.Serializable

@Serializable
data class Name(val name: String)

fun example() {
    val testModel = Name("Testing")
    val testParam = DeepLinkParam(
        "testParam",
        testModel,
        Name.serializer()
    )
    val url = Rinku.buildUrl("https://example.com/dev/path", testParam)
    Rinku.handleDeepLink(url)
}
```

`buildUrl` appends serialized JSON without percent-encoding it. This example is for
internal dispatch; percent-encode query values before sending the URL through a browser
or operating-system URL handler. Use a base URL without an existing query or fragment.
