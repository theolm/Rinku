package config

import org.gradle.api.JavaVersion

object Config {
    const val applicationId = "dev.theolm.rinku"
    const val minSdk = 23
    const val targetSdk = 35
    const val compileSdk = 36
    const val versionCode = 1
    const val versionName = "1.0.0"
    val javaVersion = JavaVersion.VERSION_17

    // Libraries versions
    const val artifactId = "rinku"
    const val groupId = "dev.theolm"
    const val libVersion = "1.4.0"
}
