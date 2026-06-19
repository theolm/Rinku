import config.Config

plugins {
    id("android-app-setup")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
}

android {
    namespace = Config.applicationId

    defaultConfig {
        applicationId = Config.applicationId + ".decompose"
    }
}

dependencies {
    implementation(projects.samples.decompose)
    implementation(projects.samples.common)
    implementation(projects.rinku.rinkuCore)
    implementation(projects.rinku.rinkuComposeExt)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.decompose)
    implementation(libs.decompose.compose)
    implementation(libs.kotlinx.serialization)
}
