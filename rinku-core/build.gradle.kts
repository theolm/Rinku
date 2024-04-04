import config.Config

plugins {
    id("android-lib-setup")
    id("compose-module-setup")
}

android {
    namespace = Config.applicationId
}


kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            api(libs.uri)
        }
    }
}
