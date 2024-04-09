import config.Config
import plugins.setupKmpTargets

plugins {
    id("android-lib-setup")
    id("compose-module-setup")
    id("detekt-setup")
}

android {
    namespace = Config.applicationId + ".common"
}

kotlin {
    setupKmpTargets()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.rinku.rinkuCore)
        }
    }
}
