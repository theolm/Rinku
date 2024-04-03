import config.Config
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

android {
    compileSdk = Config.compileSdk

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = Config.minSdk
    }

    compileOptions {
        sourceCompatibility = Config.javaVersion
        targetCompatibility = Config.javaVersion
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

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
}
