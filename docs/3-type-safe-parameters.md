# Type-safe parameters

Rinku supports get and create typesafe parameters leveraging the kotlinx-serialization. In order to use the following functions the app/module needs to setup [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization).

### Getting parameters
Use the `DeepLink` extension `<T> DeepLink.getParameter` and pass the key of the argument in the URL query and the KSerializer of the correspoinding kotlin class.

example:

```kotlin
@Serializable
data class Name(val name: String)

fun example() {
    val url = "https://theolm/dev/path?test={"name": "Theo"}"
    val deepLink = DeepLink(url)

    val param : Name = deepLink.getParameter(name = "wrong name", kSerializer = Name.serializer())
}

``` 

### Build a URL using Serializable classes
Rinku also provides the helper funcion `Rinku.buildUrl` that facilitates the creation of internal deeplinks with parameters. In order to do that you first need to create the URL and fire the deeplink.

example:

```kotlin
@Serializable
data class Name(val name: String)

fun example() {
    val testModel = Name("Testing")
    val testParam = DeepLinkParam(
        "testParam",
        testModel,
        Name.serializer()
    )
    val url = Rinku.buildUrl(TestUrl, testParam)
    Rinku.handleDeepLink(url)
}
```