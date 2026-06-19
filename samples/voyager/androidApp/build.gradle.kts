import config.Config

plugins {
    id("android-app-setup")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = Config.applicationId

    defaultConfig {
        applicationId = Config.applicationId + ".voyager"
    }
}

dependencies {
    implementation(projects.samples.voyager.shared)
    implementation(projects.samples.common)
    implementation(projects.rinku.rinkuCore)
    implementation(projects.rinku.rinkuComposeExt)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.screenModel)
    implementation(libs.kotlinx.serialization)
}
