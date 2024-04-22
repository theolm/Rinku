# Changelog

## [v0.2.0] - 2024-04-22

### Added

- **Typesafe arguments**: The library now supports typesafe arguments for deep links, making it easier to work with deep link parameters. In order to use it the app/module needs to include kotlinx-serialization. To get the parameter value, you can use the `getArgument` method on the `DeepLink` object. The method will return the parameter value or null if the parameter is not present.
- **BuildUrl**: The library now supports building URLs adding Serializable arguments. This is very handy for internal navigation. Use the `Rinku.buildUrl` to generate the url and pass it as argument to `handleDeepLink`.
