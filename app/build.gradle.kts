
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
    enableAggregatingTask = true
}
val SUPPORTED_ABIS = setOf("armeabi-v7a", "arm64-v8a", "x86")

android {
    namespace = "org.ireader.app"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        applicationId = ProjectConfig.applicationId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        resources.excludes.addAll(
            listOf(
                "META-INF/DEPENDENCIES",
                "LICENSE.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/README.md",
                "META-INF/NOTICE",
                "META-INF/*.kotlin_module",
                "META-INF/*.version",
            )
        )
    }

    buildFeatures {
        compose = true

        // Disable some unused things
        aidl = false
        renderScript = false
        shaders = false
    }
    splits {
        abi {
            isEnable = true
            reset()
            include(*SUPPORTED_ABIS.toTypedArray())
            isUniversalApk = true
        }
    }
    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
        abortOnError = false
        checkReleaseBuilds = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.extension.get()
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
    dependenciesInfo {
        includeInApk = false
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
//        create("preview") {
//            initWith(getByName("release"))
//            buildConfigField("boolean", "PREVIEW", "true")
//
//            val debugType = getByName("debug")
//            signingConfig = debugType.signingConfig
//            versionNameSuffix = debugType.versionNameSuffix
//            applicationIdSuffix = debugType.applicationIdSuffix
//        }
    }
}

dependencies {
    implementation(androidx.emoji)
    implementation(androidx.appCompat)
    implementation(androidx.core)
    implementation(androidx.material)
    implementation(androidx.media)
    implementation(composeLib.compose.activity)
    implementation("androidx.core:core-splashscreen:1.0.0-rc01")
    implementation(composeLib.material3.core)

    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.googlFonts)

    implementation(project(Modules.coreApi))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.data))

    implementation(project(Modules.domain))

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
    implementation(platform(commonLib.firebase.bom))
    implementation(commonLib.firebase.analyticKtx)
    implementation(commonLib.firebase.analytic)
    implementation(commonLib.firebase.crashlytics)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)
    implementation("com.google.firebase:firebase-analytics:21.0.0")

    /** Hilt **/
    kapt(commonLib.hilt.androidcompiler)

    implementation(commonLib.hilt.android)
    implementation(androidx.work.runtime)
    implementation(commonLib.hilt.worker)

    implementation(commonLib.jsoup)
    testImplementation(commonLib.ktor.core.cio)

    implementation(composeLib.compose.runtime)

    /** Room **/
    implementation(commonLib.room.runtime)
    implementation(commonLib.room.ktx)
    kapt(commonLib.room.compiler)

    testImplementation(test.bundles.common)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
    testImplementation(commonLib.hilt.androidtest)
    testImplementation(commonLib.room.testing)
    androidTestImplementation(commonLib.hilt.androidtest)
    androidTestImplementation(commonLib.room.testing)
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
