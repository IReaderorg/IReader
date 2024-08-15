import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id(libs.plugins.moko.get().pluginId)
    alias(libs.plugins.jetbrainCompose)
    id(libs.plugins.buildkonfig.get().pluginId)
    alias(kotlinx.plugins.compose.compiler)
}
kotlin {
    androidTarget()
    jvm() {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(libs.moko.core)
                compileOnly(compose.runtime)
                compileOnly(compose.ui)

            }
            this.kotlin.srcDirs(File(layout.buildDirectory.orNull!!.asFile, "generated/moko-resources/commonMain/src"))
        }
        androidMain {
            dependencies {
                compileOnly(compose.animationGraphics)
            }
        }
        jvmMain {
            dependencies {
                api(libs.moko.core)
            }
        }
    }

}
dependencies {
    commonMainApi(libs.resources.v0230)
//    commonMainApi("dev.icerock.moko:resources-compose:0.23.0") // for compose multiplatform
//
//    commonTestImplementation("dev.icerock.moko:resources-test:0.23.0")
}

android {
    namespace = "ireader.i18n"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    sourceSets.getByName("main") {
        assets.srcDir(File(buildDir, "generated/moko-resources/commonMain/assets"))
        res.srcDir(File(buildDir, "generated/moko-resources/commonMain/res"))
        res.srcDir("src/commonMain/moko-resources")
    }
}
tasks {
    this@tasks.registerResources(project)

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
    targetConfigs("release") {
        create("release") {
            buildConfigField(BOOLEAN, "DEBUG", "false")
        }
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
    val df: java.text.SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}

fun runCommand(command: String): String {
    val byteOut: java.io.ByteArrayOutputStream = ByteArrayOutputStream()
    project.exec {
        commandLine = command.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}
multiplatformResources {
    this.resourcesPackage = "ireader.i18n.resources"
}
