import config.Config
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("io.gitlab.arturbosch.detekt")
}

val libs = the<LibrariesForLibs>()
dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.composeRules)
}

private val projectSource = file("$projectDir/src/commonMain/kotlin")
private val configFile = files("$rootDir/config/detekt/detekt.yml")
private val kotlinFiles = "**/*.kt"
private val resourceFiles = "**/resources/**"
private val buildFiles = "**/build/**"
private val generatedFiles = "**/generated/**"
private val scriptsFiles = "**/*.kts"

detekt {
    source.from(projectSource)
    buildUponDefaultConfig = true
    parallel = true
    autoCorrect = false
    ignoreFailures = false
    config.setFrom(configFile)
}

tasks.withType<Detekt>().configureEach {
    include(kotlinFiles)
    exclude(resourceFiles, buildFiles, generatedFiles, scriptsFiles)
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = Config.javaVersion.toString()
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = Config.javaVersion.toString()
}
