import config.Config

plugins {
    id("sample-setup")
    id("kotlin-parcelize")
}

android {
    defaultConfig {
        applicationId = Config.applicationId + ".decompose"
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(projects.rinku.rinkuCore)
            implementation(libs.decompose)
            implementation(libs.decompose.compose)
        }

        iosMain.dependencies {
            api(projects.rinku.rinkuCore)
        }
    }
}
