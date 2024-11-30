# Rinku: Deep Link Handling for Kotlin Multiplatform
[![Maven Central](https://img.shields.io/maven-central/v/dev.theolm/rinku)](https://mvnrepository.com/artifact/dev.theolm)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/theolm/rinku)
[![GitHub License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://opensource.org/licenses/MIT)

Rinku is a lightweight Kotlin Multiplatform library designed to simplify deep link handling across iOS and Android platforms. By abstracting platform-specific details, Rinku enables developers to manage deep links in a unified manner, enhancing code reuse and maintaining consistency across platforms.


## Core Features

- **Unified Deep Link Management:** Architect your deep link logic once, and execute seamlessly across iOS and Android platforms.
- **Simplified Integration:** Designed for rapid setup with minimal configuration requirements, ensuring a smooth start.
- **Universal Compatibility with Navigation Libraries:** Rinku's architecture is designed for compatibility with any navigation library. It delegates the parsing and navigation logic to the application, permitting seamless integration and enhanced flexibility.

## Prerequisites

- Kotlin Multiplatform project setup
- For Android: Minimum SDK version 22
- For iOS: iOS 13.0 or later

The library is available via Maven Central
```kt
implementation("dev.theolm:rinku:<latest_version>")
implementation("dev.theolm:rinku-compose-ext:<latest_version>")
```

## Documentation
You can find the full documentation [here](https://theolm.dev/Rinku/)


## License
Rinku is released under the MIT License. See the LICENSE file for more details.