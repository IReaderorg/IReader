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
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName
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
        kotlinCompilerExtensionVersion = compose.versions.compose.get()
    }
}




dependencies {
    implementation("androidx.core:core-splashscreen:1.0.0-beta01")


    implementation(androidx.emoji)
    implementation(androidx.material)

    implementation(project(Modules.coreUi))
    implementation(project(Modules.data))
    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.presentation))

    implementation(libs.tachiyomi)

    implementation(compose.compose.material)
    implementation(compose.compose.hiltNavigation)


    implementation(libs.timber)
    implementation(accompanist.systemUiController)
    implementation(accompanist.navAnimation)
    implementation(accompanist.navMaterial)


    testImplementation(test.junit4)
    testImplementation(test.extJunit)
    testImplementation(test.espresso)


    /** LifeCycle **/
    testImplementation(androidx.lifecycle.runtime)
    testImplementation(androidx.lifecycle.viewModel)


    /** Firebase **/
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analyticKtx)
    implementation(libs.firebase.analytic)
    implementation(libs.firebase.crashlytics)


    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)


    /** Hilt **/
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.androidcompiler)
    implementation(libs.hilt.android)
    implementation(androidx.work.runtime)
    implementation(libs.hilt.worker)
    implementation(libs.hilt.androidtest)
    androidTestImplementation(libs.hilt.androidtest)


    /** Room **/
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    kapt(libs.room.compiler)


    /** Coil **/
    implementation(compose.compose.coil)

    /** Moshi **/
    implementation(libs.moshi.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.codegen)


    /** Network Client - OkHttp**/
    implementation(libs.okio)
    implementation(libs.okhttp.okhttp3)
    implementation(libs.okhttp.interceptor)


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
//configurations.all {
//    resolutionStrategy.dependencySubstitution {
//        substitute(module("org.tachiyomi:core-desktop:1.2-SNAPSHOT"))
//            .using(module("org.tachiyomi:core-jvm:1.2-SNAPSHOT"))
//            .withoutClassifier()
//    }
//}
