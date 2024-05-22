package plugins

import config.Config
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

fun KotlinMultiplatformExtension.setupKmpTargets(
    onBinariesFramework: (Framework) -> Unit = {}
) {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = Config.javaVersion.toString()
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            onBinariesFramework(this)
        }
    }
}
