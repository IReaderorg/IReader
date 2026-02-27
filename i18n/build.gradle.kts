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
    
    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "i18n"
            isStatic = true
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
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
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
// IMPORTANT: Only compute these for release builds to avoid cache invalidation
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

// Detect if this is a release build based on Gradle task names
val isReleaseBuild: Boolean = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("release", ignoreCase = true) || 
    taskName.contains("Release") ||
    taskName.contains("assemble") && !taskName.contains("debug", ignoreCase = true)
}

// Only compute build time for release builds to avoid cache invalidation
val buildTime: Provider<String> = provider {
    if (isReleaseBuild) {
        val df: java.text.SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
        df.timeZone = TimeZone.getTimeZone("UTC")
        df.format(Date())
    } else {
        "dev-build"
    }
}

buildkonfig {
    packageName = "ireader.i18n"
    exposeObjectWithName = "BuildKonfig"
    defaultConfigs {
        buildConfigField(BOOLEAN, "DEBUG", isReleaseBuild.not().toString())
        // Only include dynamic values for release builds
        if (isReleaseBuild) {
            buildConfigField(STRING, "COMMIT_COUNT", "\"${commitCount.get()}\"")
            buildConfigField(STRING, "COMMIT_SHA", "\"${gitSha.get()}\"")
            buildConfigField(STRING, "BUILD_TIME", "\"${buildTime.get()}\"")
        } else {
            // Use static values for debug builds to enable caching
            buildConfigField(STRING, "COMMIT_COUNT", "\"dev\"")
            buildConfigField(STRING, "COMMIT_SHA", "\"dev\"")
            buildConfigField(STRING, "BUILD_TIME", "\"dev-build\"")
        }
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
    // Only generate resource accessors when needed (skip during fast development builds)
    generateResClass = when {
        project.hasProperty("skipI18n") -> org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration.Never
        else -> org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration.Auto
    }
}
