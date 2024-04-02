package config

import org.gradle.api.JavaVersion

object Config {
    const val applicationId = "com.theolm.temp"
    const val minSdk = 22
    const val targetSdk = 34
    const val compileSdk = 34
    const val versionCode = 1
    const val versionName = "1.0.0"
    const val packageVersion = versionName

    val javaVersion = JavaVersion.VERSION_17
}