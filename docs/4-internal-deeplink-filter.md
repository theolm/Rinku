# Internal Deeplink Filter

Rinku provides a simple way to filter unwanted external deeplinks.
Instead of filter deeplinks providing specific paths in the AndroidManifest and info.plist you implment the interface `DeepLinkFilter` and pass it into Rinku initialization. With this configuration, when the app recieves a not valid deeplink rinku is not going to handle it. This is usefull to block internal deeplinks from external access without having to include it in platform specific configuration.

### Deeplink mapper
This feature is used to map external deeplinks into internal deeplinks.
Use case 1: Android and ios application has different deeplinks registered in marketing campaigns. The mapper can be used to map the external deeplink to unique internal deeplink, and the application can handle the deeplink accordingly in a unified way.
Use case 2: External deeplink does not have the full path to represent a valid stack. Use the mapper to provide the full stack.

```kotlin
// External deeplink rinku://dev.theolm/screenC/
// The deeplink is missing A and B

// Implement and pass the mapper into Rinku init. This way the external deeplink will be mapped and can be handle in the commonMain.
object ExampleMapper : DeepLinkMapper {
    override fun map(url: String): String {
        return if (url == "rinku://dev.theolm/screenC/") {
            return "rinku://dev.theolm/screenA/screenB/screenC/"
        } else {
            url
        }
    }
}
```