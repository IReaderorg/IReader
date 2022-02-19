
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
        testInstrumentationRunner = "org.ireader.infinity.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Compose.composeVersion
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
}
tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += "-opt-in=org.mylibrary.OptInAnnotation"
    }
}


dependencies {
    implementation("androidx.core:core-splashscreen:1.0.0-beta01")
    implementation(project(Modules.coreUi))
    implementation(project(Modules.data))
    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.presentation))
    implementation(project(Modules.source))

    implementation(AndroidX.coreKtx)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.webkit)
    implementation(AndroidX.browser)
    implementation(AndroidX.material)
    implementation(AndroidX.activity)
    implementation(AndroidX.appStartUpRuntime)
    implementation("androidx.test:runner:1.4.0")

    testImplementation(Testing.junit4)
    androidTestImplementation(Testing.extJunit)
    androidTestImplementation(Testing.espresso)

    /** Compose **/
    implementation(Compose.compiler)
    implementation(Compose.foundation)
    implementation(Compose.activityCompose)
    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.uiToolingPreview)
    implementation(Compose.viewModelCompose)
    implementation(Compose.icons)
    implementation(Compose.animations)
    implementation(Compose.navigation)
    implementation(Compose.hiltNavigationCompose)
    debugImplementation(Compose.ui_test_manifest)
    androidTestImplementation(Compose.testing)
    androidTestImplementation(Compose.ui_test_manifest)
    debugImplementation(Compose.composeTooling)


//    /** Accompanist **/
    implementation(Accompanist.systemUiController)
    implementation(Accompanist.webView)
    implementation(Accompanist.swipeRefresh)
    implementation(Accompanist.pager)
    implementation(Accompanist.pagerIndicator)
    implementation(Accompanist.insets)
    implementation(Accompanist.navAnimation)
    implementation(Accompanist.flowlayout)
    implementation(Accompanist.navMaterial)


    /** LifeCycle **/
    implementation(LifeCycle.runtimeKtx)
    implementation(LifeCycle.viewModel)


    /** Firebase **/
    implementation(platform("com.google.firebase:firebase-bom:29.0.4"))
    implementation(Firebase.analyticKtx)
    implementation(Firebase.analytic)
    implementation(Firebase.crashlytics)


    /** Coroutine **/
    implementation(Coroutines.core)
    implementation(Coroutines.android)

    implementation(Worker.runtimeKtx)


    /** Hilt **/
    implementation(DaggerHilt.hiltAndroid)
    kapt(DaggerHilt.hiltAndroidCompiler)
    kaptTest(DaggerHilt.hiltAndroidCompiler)
    kaptAndroidTest(DaggerHilt.hiltAndroidCompiler)
    kapt(DaggerHilt.hiltCompiler)
    implementation(DaggerHilt.worker)
    testImplementation(DaggerHilt.hiltAndroidTest)
    androidTestImplementation(DaggerHilt.hiltAndroidTest)


    /** Room **/
    implementation(Room.roomRuntime)
    kapt(Room.roomCompiler)
    implementation(Room.roomKtx)
    implementation(Room.roomPaging)

    /** Coil **/
    implementation(Coil.coilCompose)

//
    /** Moshi **/
    implementation(Moshi.moshi)
    kapt(Moshi.moshiCodegen)
    implementation(Moshi.moshiKotlin)

    /** Timber **/
    implementation(Timber.timber)

//    /** Network Client - OkHttp**/
    implementation(OkHttp.okHttp3)
    implementation(OkHttp.okio)
    implementation(OkHttp.okHttp3Interceptor)



    testImplementation(Testing.junit4)
    testImplementation(Testing.junitAndroidExt)
    testImplementation(Testing.truth)
    testImplementation(Testing.coroutines)
    testImplementation(Testing.composeUiTest)


    androidTestImplementation(Testing.junit4)
    androidTestImplementation(Testing.junitAndroidExt)
    androidTestImplementation(Testing.truth)
    androidTestImplementation(Testing.coroutines)
    androidTestImplementation(Testing.composeUiTest)
    androidTestImplementation(Testing.hiltTesting)
    androidTestImplementation(Testing.testRunner)
    // Instrumented Unit Tests
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")

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
