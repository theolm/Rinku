import config.Config
import plugins.setupKmpTargets

plugins {
    id("android-lib-setup")
    id("compose-module-setup")
}

android {
    namespace = Config.applicationId
}


kotlin {
    setupKmpTargets()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            api(libs.uri)
        }
    }
}
