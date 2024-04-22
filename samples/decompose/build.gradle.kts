import config.Config

plugins {
    id("sample-setup")
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
            implementation(libs.decompose)
            implementation(libs.decompose.compose)
            implementation(libs.kotlinx.serialization)
        }
    }
}
