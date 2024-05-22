import config.Config
import plugins.setupKmpTargets

plugins {
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("android-lib-setup")
    id("detekt-setup")
}

android {
    namespace = Config.applicationId + ".common"
}

kotlin {
    setupKmpTargets()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.rinku.rinkuCore)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization)
        }
    }
}
