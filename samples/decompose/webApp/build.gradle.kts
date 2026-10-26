import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                outputFileName = "rinku-decompose-web.js"
            }
        }
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "rinku-decompose-web.js"
            }
        }
        binaries.executable()
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.samples.decompose.shared)
            implementation(projects.samples.common)
            implementation(compose.ui)
            implementation(libs.kotlinx.browser)
        }
    }
}
