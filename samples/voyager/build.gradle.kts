import config.Config
import plugins.setupKmpTargets

plugins {
    id("sample-setup")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("detekt-setup")
    alias(libs.plugins.kotlinSerialization)
}

android {
    defaultConfig {
        applicationId = Config.applicationId + ".voyager"
    }
}

kotlin {
    setupKmpTargets(
        onBinariesFramework = {
            it.export(projects.rinku.rinkuCore)
            it.baseName = "ComposeApp"
            it.isStatic = true
        }
    )

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            api(projects.rinku.rinkuCore)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(projects.rinku.rinkuComposeExt)
            implementation(projects.samples.common)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.kotlinx.serialization)
        }
    }
}
