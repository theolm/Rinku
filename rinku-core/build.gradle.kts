import config.Config
import plugins.setupKmpTargets

plugins {
    id("android-kmp-lib-setup")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    id("publish-setup")
    id("detekt-setup")
}

kotlin {
    setupKmpTargets()

    android {
        compileSdk = Config.compileSdk
        minSdk = Config.minSdk
        namespace = Config.applicationId + ".core"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.ktx)
        }
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlin.test.common)
            implementation(libs.kotlin.test.annotation)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    val version = System.getenv("VERSION") ?: Config.libVersion
    coordinates(
        groupId = Config.groupId,
        artifactId = Config.artifactId,
        version = version
    )

    pom {
        name.set("Rinku")
        description.set("Deep Link Handling for Kotlin Multiplatform.")
    }
}
