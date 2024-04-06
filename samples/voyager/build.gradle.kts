import config.Config
import plugins.setupKmpTargets

plugins {
    id("sample-setup")
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
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
        }
    }
}
