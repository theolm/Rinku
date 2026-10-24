import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()
    jvmToolchain(17)
    sourceSets {
        jvmMain.dependencies {
            implementation(projects.samples.voyager.shared)
            implementation(projects.rinku.rinkuCore)
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RinkuVoyager"
            packageVersion = "1.0.0"
            modules("java.instrument", "java.management", "jdk.unsupported")
            macOS {
                bundleID = "dev.theolm.rinku.sample.voyager"
                infoPlist {
                    extraKeysRawXml = """
                        <key>CFBundleURLTypes</key>
                        <array><dict>
                            <key>CFBundleTypeRole</key><string>Editor</string>
                            <key>CFBundleURLName</key><string>dev.theolm.rinku.sample.voyager</string>
                            <key>CFBundleURLSchemes</key><array><string>rinku-voyager</string></array>
                        </dict></array>
                    """.trimIndent()
                }
            }
        }
    }
}
