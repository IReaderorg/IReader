import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrainCompose)
    id(libs.plugins.buildkonfig.get().pluginId)
    alias(kotlinx.plugins.compose.compiler)
}
kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.components.resources)
            }
        }
        androidMain {
            dependencies {
                implementation(compose.animationGraphics)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.components.resources)
            }
        }
    }

}

android {
    namespace = "ireader.i18n"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
    }
    lint {
        targetSdk = ProjectConfig.targetSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
}
// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
// Cached to avoid running git commands on every configuration
val commitCount: Provider<String> = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim().takeIf { it.isNotEmpty() } ?: "unknown" }
    .orElse("unknown")

val gitSha: Provider<String> = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim().takeIf { it.isNotEmpty() } ?: "unknown" }
    .orElse("unknown")

val buildTime: Provider<String> = provider {
    val df: java.text.SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    df.format(Date())
}

buildkonfig {
    packageName = "ireader.i18n"
    exposeObjectWithName = "BuildKonfig"
    defaultConfigs {
        buildConfigField(BOOLEAN, "DEBUG", "true")
        buildConfigField(STRING, "COMMIT_COUNT", "\"${commitCount.get()}\"")
        buildConfigField(STRING, "COMMIT_SHA", "\"${gitSha.get()}\"")
        buildConfigField(STRING, "BUILD_TIME", "\"${buildTime.get()}\"")
        buildConfigField(BOOLEAN, "INCLUDE_UPDATER", "false")
        buildConfigField(BOOLEAN, "PREVIEW", "false")
        buildConfigField(STRING, "VERSION_NAME", "\"${ProjectConfig.versionName}\"")
        buildConfigField(INT, "VERSION_CODE", "${ProjectConfig.versionCode}")
    }
}

// Configure Compose Multiplatform Resources
compose.resources {
    publicResClass = true
    packageOfResClass = "ireader.i18n.resources"
    generateResClass = org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration.Auto
}
