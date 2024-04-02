plugins {
    id("android-application-setup")
    id("compose-module-setup")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.koin)
        }
    }
}

android {
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}
