import config.Config

plugins {
    id("android-lib-setup")
    id("compose-module-setup")
}

android {
    namespace = Config.applicationId
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.koin)
        }
    }
}
