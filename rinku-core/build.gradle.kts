import com.vanniktech.maven.publish.SonatypeHost
import config.Config
import plugins.setupKmpTargets

plugins {
    id("android-lib-setup")
    id("compose-module-setup")
//    id("publish-setup")
    id("detekt-setup")
}

android {
    namespace = Config.applicationId
}


kotlin {
    setupKmpTargets()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            api(libs.uri)
        }
    }
}

//mavenPublishing {
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
//    signAllPublications()
//
//    val version = System.getenv("VERSION") ?: Config.libVersion
//    coordinates(
//        groupId = Config.groupId,
//        artifactId = artifactId,
//        version = version
//    )
//
//    pom {
//        name.set("Txt Log Writer")
//        description.set("It's a simple Kermit log writer that writes logs to a text file.")
//    }
//}