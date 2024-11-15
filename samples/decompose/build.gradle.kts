import config.Config

plugins {
    id("sample-setup")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
    id("detekt-setup")
    alias(libs.plugins.kotlinSerialization)
}

android {
    defaultConfig {
        applicationId = Config.applicationId + ".decompose"
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = Config.javaVersion.toString()
            }
        }
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(projects.rinku.rinkuCore)
            implementation(projects.rinku.rinkuComposeExt)
            implementation(projects.samples.common)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.decompose)
            implementation(libs.decompose.compose)
            implementation(libs.kotlinx.serialization)
        }
    }
}
