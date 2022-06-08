
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization")
    id("com.google.firebase.crashlytics")
}
hilt {
    enableExperimentalClasspathAggregation = true
}
android {
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        applicationId = ProjectConfig.applicationId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName
    }
    packagingOptions {
        resources.excludes.addAll(
            listOf(
                "LICENSE.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/README.md",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "**/attach_hotspot_windows.dll",
                "META-INF/licenses/ASM",
                "META-INF/gradle/incremental.annotation.processors",
                "META-INF/DEPENDENCIES",
                "mozilla/public-suffix-list.txt",
            )
        )
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compose.get()
    }
    defaultConfig {
        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
        buildConfigField("boolean", "INCLUDE_UPDATER", "false")
        buildConfigField("boolean", "PREVIEW", "false")
        buildConfigField("String", "VERSION_NAME", "\"${ProjectConfig.versionName}\"")
        buildConfigField("int", "VERSION_CODE",  "${ProjectConfig.versionCode}")
    }


    buildTypes {
        named("debug") {
            versionNameSuffix = "-${getCommitCount()}"
            applicationIdSuffix = ".debug"
            extra["enableCrashlytics"] = false
            extra["alwaysUpdateBuildId"] = false
            isCrunchPngs = false
        }
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
        create("preview") {
            initWith(getByName("release"))
            buildConfigField("boolean", "PREVIEW", "true")

            val debugType = getByName("debug")
            signingConfig = debugType.signingConfig
            versionNameSuffix = debugType.versionNameSuffix
            applicationIdSuffix = debugType.applicationIdSuffix
        }
    }
}

dependencies {
    implementation(androidx.emoji)
    implementation(androidx.appCompat)
    implementation(androidx.core)
    implementation(androidx.material)
    implementation(androidx.media)
    implementation(compose.compose.activity)
    implementation("androidx.core:core-splashscreen:1.0.0-beta02")
implementation(compose.compose.material3)


    implementation(compose.compose.coil)
    implementation(compose.compose.googlFonts)

    implementation(project(Modules.coreApi))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.data))

    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.presentation))
    implementation(project(Modules.commonResources))
    implementation(project(Modules.commonModels))
    implementation(project(Modules.commonData))
    implementation(project(Modules.uiLibrary))
    implementation(project(Modules.coreCatalogs))
    implementation(project(Modules.commonExtensions))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.uiSources))

    /** Firebase **/
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analyticKtx)
    implementation(libs.firebase.analytic)
    implementation(libs.firebase.crashlytics)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)
    implementation("com.google.firebase:firebase-analytics:21.0.0")

    /** Hilt **/
    kapt(libs.hilt.androidcompiler)

    implementation(libs.hilt.android)
    implementation(androidx.work.runtime)
    implementation(libs.hilt.worker)

    implementation(compose.compose.runtime)

    /** Room **/
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    testImplementation(test.bundles.common)
    testImplementation(libs.hilt.androidtest)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.hilt.androidtest)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(test.bundles.common)
}

kapt {
    correctErrorTypes = true
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
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}

fun runCommand(command: String): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        commandLine = command.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}
