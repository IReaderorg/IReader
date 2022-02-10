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
}


android {
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        applicationId = ProjectConfig.applicationId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }


    buildTypes {
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
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
        kotlinCompilerExtensionVersion = Compose.composeCompilerVersion
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
    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions.freeCompilerArgs += "-opt-in=org.mylibrary.OptInAnnotation"
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

}

dependencies {
    implementation(project(Modules.coreUi))
    implementation(project(Modules.data))
    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.presentation))


    implementation(AndroidX.coreKtx)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.webkit)
    implementation(AndroidX.browser)
    implementation(AndroidX.material)
    implementation(AndroidX.activity)
    implementation(AndroidX.appStartUpRuntime)

    testImplementation(Testing.junit4)
    androidTestImplementation(Testing.extJunit)
    androidTestImplementation(Testing.espresso)

    /** Compose **/
    implementation(Compose.compiler)
    implementation(Compose.foundation)
//    implementation(Compose.activityCompose)
    implementation(Compose.ui)
    implementation(Compose.material)
//    implementation(Compose.uiToolingPreview)
//    implementation(Compose.viewModelCompose)
//    implementation(Compose.icons)
//    implementation(Compose.animations)
    implementation(Compose.navigation)
    implementation(Compose.hiltNavigationCompose)
//    androidTestImplementation(Compose.testing)
//    debugImplementation(Compose.composeTooling)
//    implementation(Compose.paging)

//    /** Accompanist **/
//    implementation(Accompanist.systemUiController)
//    implementation(Accompanist.webView)
//    implementation(Accompanist.swipeRefresh)
//    implementation(Accompanist.pager)
//    implementation(Accompanist.pagerIndicator)
    implementation(Accompanist.insets)
//    implementation(Accompanist.navAnimation)
//    implementation(Accompanist.flowlayout)
//    implementation(Accompanist.navMaterial)


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
    kapt(DaggerHilt.hiltCompiler)
    implementation(DaggerHilt.worker)


    /** Room **/
    implementation(Room.roomRuntime)
    kapt(Room.roomCompiler)
    implementation(Room.roomKtx)
    implementation(Room.roomPaging)

    /** Coil **/
    implementation(Coil.coilCompose)

//    /** JSoup **/
//    implementation(Jsoup.jsoup)

//    /** DataStore **/
//    implementation(Datastore.datastore)
//    implementation(Datastore.core)

//    /** Retrofit **/
//    implementation(Retrofit.retrofit)
//    implementation(Retrofit.moshiConverter)

//
//    /** Gson **/
//    implementation(Gson.gson)
//    implementation(Gson.gsonConvertor)
//
//
    /** Moshi **/
    implementation(Moshi.moshi)
    kapt(Moshi.moshiCodegen)
    implementation(Moshi.moshiKotlin)

    /** Timber **/
    implementation(Timber.timber)

//    /** Network Client - OkHttp**/
    implementation(OkHttp.okHttp3)
    implementation(OkHttp.okHttp3Interceptor)
//    implementation(OkHttp.okhttp3_doh)
//    implementation(OkHttp.okio)

//    implementation(Jsonpathkt.jsonpathkt)
    implementation(FlowPreferences.flowPreferences)


//    implementation(Kotlin.jsonSerialization)


    implementation(Koin.koinWorkManager)
    implementation(Koin.koinNavigation)
    implementation(Koin.koinCompose)




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

