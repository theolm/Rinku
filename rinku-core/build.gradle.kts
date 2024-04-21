import com.vanniktech.maven.publish.SonatypeHost
import config.Config
import plugins.setupKmpTargets

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    id("android-lib-setup")
    id("publish-setup")
    id("detekt-setup")
}

android {
    namespace = Config.applicationId
}

kotlin {
    setupKmpTargets()

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
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
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
