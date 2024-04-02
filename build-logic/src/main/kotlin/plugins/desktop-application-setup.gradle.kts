import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import config.Config

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = Config.applicationId
            packageVersion = Config.packageVersion
        }
    }
}