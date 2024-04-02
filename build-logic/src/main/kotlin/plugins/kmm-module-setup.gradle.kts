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
        targetSdk = Config.targetSdk
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
                jvmTarget = "17"
            }
        }
    }

    jvm("desktop")

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

    val libs = the<LibrariesForLibs>()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.test)
        }

        iosMain.dependencies {
            //Workaround to fix koin on ios
            implementation(libs.stately.common)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(libs.kotlin.coroutines.swing)
        }
    }
}
