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
    androidTarget()
    jvm() {
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
        jvmMain {
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
fun runCommand(command: String): String {
    return try {
        val process = Runtime.getRuntime().exec(command.split(" ").toTypedArray())
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        "unknown"
    }
}

fun getCommitCount(): String {
    return runCommand("git rev-list --count HEAD")
    // return "1"
}

fun getGitSha(): String {
    return runCommand("git rev-parse --short HEAD")
    // return "1"
}

fun getBuildTime(): String {
    val df: java.text.SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}

buildkonfig {
    packageName = "ireader.i18n"
    exposeObjectWithName = "BuildKonfig"
    defaultConfigs {
        buildConfigField(BOOLEAN, "DEBUG", "true")
        buildConfigField(STRING, "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField(STRING, "COMMIT_SHA", "\"${getGitSha()}\"")
        buildConfigField(STRING, "BUILD_TIME", "\"${getBuildTime()}\"")
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
    generateResClass = org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration.Always
}
