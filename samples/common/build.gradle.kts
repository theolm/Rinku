import config.Config
import plugins.setupKmpTargets

plugins {
    id("android-kmp-lib-setup")
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("detekt-setup")
}

kotlin {
    setupKmpTargets()

    android {
        compileSdk = Config.compileSdk
        minSdk = Config.minSdk
        namespace = Config.applicationId + ".sample.common"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.rinku.rinkuCore)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization)
        }
    }
}
