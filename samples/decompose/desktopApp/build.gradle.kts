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
            implementation(projects.samples.decompose.shared)
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
            packageName = "RinkuDecompose"
            packageVersion = "1.0.0"
            modules("java.instrument", "java.management", "jdk.unsupported")
            macOS {
                bundleID = "dev.theolm.rinku.sample.decompose"
                infoPlist {
                    extraKeysRawXml = """
                        <key>CFBundleURLTypes</key>
                        <array><dict>
                            <key>CFBundleTypeRole</key><string>Editor</string>
                            <key>CFBundleURLName</key><string>dev.theolm.rinku.sample.decompose</string>
                            <key>CFBundleURLSchemes</key><array><string>rinku-decompose</string></array>
                        </dict></array>
                    """.trimIndent()
                }
            }
        }
    }
}
