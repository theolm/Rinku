import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
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
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.android)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.koin.core)
            implementation(libs.koin.test)
            implementation(libs.koin.compose)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlin.coroutines.swing)
        }

        iosMain.dependencies {
            //Workaround to fix koin on ios
            implementation(libs.stately.common)
        }
    }
}
