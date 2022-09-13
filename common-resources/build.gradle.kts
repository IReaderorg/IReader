import org.jetbrains.compose.compose
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.gradle.plugin.idea-ext")
}
kotlin {
    android()
    jvm("desktop")
    sourceSets {
        named("commonMain") {
            dependencies {
                compileOnly(compose.runtime)
                compileOnly(compose.ui)
                api(libs.moko.core)
            }
        }
        named("androidMain") {
        }
        named("desktopMain") {
        }
    }

}


android {
    namespace = "ireader.common.resources"
    sourceSets {
        named("main") {
            res.srcDir("src/commonMain/resources")
        }
    }
    defaultConfig {
        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
        buildConfigField("boolean", "INCLUDE_UPDATER", "false")
        buildConfigField("boolean", "PREVIEW", "false")
        buildConfigField("String", "VERSION_NAME", "\"${ProjectConfig.versionName}\"")
        buildConfigField("int", "VERSION_CODE", "${ProjectConfig.versionCode}")
    }
}

// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
fun getCommitCount(): String {
    return runCommand("git rev-list --count HEAD")
    // return "1"
}

fun getGitSha(): String {
    return runCommand("git rev-parse --short HEAD")
    // return "1"
}

fun getBuildTime(): String {
    val df : java.text.SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}

fun runCommand(command: String): String {
    val byteOut : java.io.ByteArrayOutputStream = ByteArrayOutputStream()
    project.exec {
        commandLine = command.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}
idea {
    module {
        (this as ExtensionAware).configure<org.jetbrains.gradle.ext.ModuleSettings> {
            (this as ExtensionAware).configure<org.jetbrains.gradle.ext.PackagePrefixContainer> {
                arrayOf(
                    "src/commonMain/kotlin",
                    "src/androidMain/kotlin",
                    "src/desktopMain/kotlin",
                    "src/jvmMain/kotlin"
                ).forEach { put(it, "ireader.common.resources") }
            }
        }
    }
}
