import com.vanniktech.maven.publish.SonatypeHost
import config.Config
import plugins.setupKmpTargets

plugins {
    id("android-lib-setup")
    id("compose-module-setup")
    id("publish-setup")
    id("detekt-setup")
}

android {
    namespace = Config.applicationId + ".compose.ext"
}

kotlin {
    setupKmpTargets()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(projects.rinku.rinkuCore)
            implementation(libs.kotlinx.coroutines.core)
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
        artifactId = Config.artifactId + "-compose-ext",
        version = version
    )

    pom {
        name.set("Rinku Compose Extensions")
        description.set("Compose extensions to facilitate the usage of Rinku in Compose multiplatform projects.")
    }
}
