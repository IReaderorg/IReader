
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization")
    id("com.google.firebase.crashlytics")
    id("com.github.ben-manes.versions")
}


android {
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        applicationId = ProjectConfig.applicationId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.ConfigVersionCode
        versionName = ProjectConfig.ConfigVersionName
    }
    packagingOptions {
        resources.excludes.addAll(listOf(
            "LICENSE.txt",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/README.md",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1",
            "**/attach_hotspot_windows.dll",
            "META-INF/licenses/ASM",
            "META-INF/gradle/incremental.annotation.processors"
        ))
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Deps.Compose.composeVersion
    }
}


addCompose()

dependencies {
    implementation("androidx.core:core-splashscreen:1.0.0-beta01")
    implementation(project(Modules.coreUi))
    implementation(project(Modules.data))
    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.presentation))
    implementation(project(Modules.source))
    implementation(Deps.Timber.timber)
    implementation(Deps.Accompanist.insets)
    implementation(Deps.Accompanist.systemUiController)
    implementation(Deps.Accompanist.navAnimation)
    implementation(Deps.Accompanist.navMaterial)


    testImplementation(Deps.Testing.junit4)
    androidTestImplementation(Deps.Testing.extJunit)
    androidTestImplementation(Deps.Testing.espresso)


    /** LifeCycle **/
    implementation(Deps.LifeCycle.runtimeKtx)
    implementation(Deps.LifeCycle.viewModel)


    /** Firebase **/
    implementation(platform("com.google.firebase:firebase-bom:29.0.4"))
    implementation(Deps.Firebase.analyticKtx)
    implementation(Deps.Firebase.analytic)
    implementation(Deps.Firebase.crashlytics)


    /** Coroutine **/
    implementation(Deps.Coroutines.core)
    implementation(Deps.Coroutines.android)

    implementation(Deps.Worker.runtimeKtx)


    /** Hilt **/
    implementation(Deps.DaggerHilt.hiltAndroid)
    kapt(Deps.DaggerHilt.hiltAndroidCompiler)
    kaptTest(Deps.DaggerHilt.hiltAndroidCompiler)
    kaptAndroidTest(Deps.DaggerHilt.hiltAndroidCompiler)
    kapt(Deps.DaggerHilt.hiltCompiler)
    implementation(Deps.DaggerHilt.worker)
    testImplementation(Deps.DaggerHilt.hiltAndroidTest)
    androidTestImplementation(Deps.DaggerHilt.hiltAndroidTest)


    /** Room **/
    implementation(Deps.Room.roomRuntime)
    kapt(Deps.Room.roomCompiler)
    implementation(Deps.Room.roomKtx)

    /** Coil **/
    implementation(Deps.Coil.coilCompose)

//
    /** Moshi **/
    implementation(Deps.Moshi.moshi)
    kapt(Deps.Moshi.moshiCodegen)
    implementation(Deps.Moshi.moshiKotlin)


//    /** Network Client - OkHttp**/
    implementation(Deps.OkHttp.okHttp3)
    implementation(Deps.OkHttp.okio)
    implementation(Deps.OkHttp.okHttp3Interceptor)

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
allprojects {
    configurations.all {
        resolutionStrategy.force("org.objenesis:objenesis:2.6")
    }
}
