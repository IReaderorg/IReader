import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id(libs.plugins.buildkonfig.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
}
kotlin {
    androidTarget()
    jvm() {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(compose.runtime)
                compileOnly(compose.ui)
                implementation(libs.lyricist.library)

            }
        }
        val androidMain by getting {
            dependencies {
                compileOnly(compose.animationGraphics)
            }
        }
        val jvmMain by getting {
            dependencies {

            }
        }
    }

}

ksp {
    // Required
    arg(
        "lyricist.xml.resourcesPath",
        kotlin.sourceSets.findByName("androidMain")!!.resources.srcDirs.first().absolutePath.replace(
            "androidMain\\resources",
            "androidMain\\res"
        )
    )

    // Optional
    arg("lyricist.packageName", "ireader.i18n")
    arg("lyricist.xml.moduleName", "xml")
    arg("lyricist.xml.defaultLanguageTag", "en")
    arg("lyricist.internalVisibility", "true")
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name.startsWith("compileDebugKotlinAndroid") || name == "compileReleaseKotlinAndroid") { // the remaining suffix is the target eg simulator, arm64, etc
        dependsOn("kspKotlinJvm")
    }
}
kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
}
dependencies {
    add("kspJvm", "cafe.adriel.lyricist:lyricist-processor:1.4.2")
    add("kspJvm", "io.github.kazemcodes:lyricist-processor-xml:1.4.3")
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
        res.srcDir("src/commonMain/resources")
        res.srcDir("src/androidMain/res")
    }
}
tasks {
    //  this@tasks.registerResources(project)

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
