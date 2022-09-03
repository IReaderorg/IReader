plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "org.ireader.domain"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.compiler.get()
    }
}

dependencies {

    implementation("nl.siegmann.epublib:epublib-core:3.1") {
        exclude(group = "org.slf4j")
        exclude(group = "xmlpull")
    }
    implementation("org.slf4j:slf4j-android:1.7.25")


    implementation(project(Modules.coreUi))
    implementation(project(Modules.commonData))
    implementation(project(Modules.commonResources))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.commonExtensions))
    implementation(project(Modules.coreCatalogs))

    implementation(project(Modules.coreApi))

    implementation(androidx.media)
    implementation(kotlinx.serialization.protobuf)
    implementation(kotlinx.datetime)
    implementation(composeLib.compose.activity)
    implementation(composeLib.compose.ui)
    implementation(composeLib.compose.runtime)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.material3.core)
    implementation(commonLib.googleTranslator)

    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.paging)
    implementation(commonLib.gson)
    implementation(project(mapOf("path" to ":common-models")))
    debugImplementation(composeLib.compose.uiTestManifest)

    implementation(androidx.work.runtime)
    implementation(commonLib.hilt.worker)

    /** Room **/
    implementation(commonLib.room.runtime)
    implementation(commonLib.room.ktx)
    // implementation(commonLib.room.paging)
    kapt(commonLib.room.compiler)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)

    implementation(commonLib.okhttp.okhttp3)
    implementation(commonLib.okhttp.interceptor)
    implementation(commonLib.okhttp.doh)

    implementation(commonLib.okhttp.doh)

    implementation(commonLib.okio)
    implementation(commonLib.jsoup)

    implementation(androidx.dataStore)

    implementation(androidx.core)
    implementation(androidx.appCompat)
    implementation(androidx.webkit)
    implementation(androidx.browser)
    implementation(androidx.material)
    implementation(kotlinx.serialization.json)
    implementation(kotlinx.reflect)

    kapt(commonLib.hilt.androidcompiler)
    kapt(commonLib.hilt.compiler)
    implementation(commonLib.hilt.android)

    /** LifeCycle **/
    //  implementation(kotlinx.lifecycle.viewModel)
    implementation(androidx.lifecycle.runtime)

    implementation(kotlinx.stdlib)
    implementation(commonLib.ktor.core)
    implementation(commonLib.ktor.contentNegotiation)
    implementation(commonLib.ktor.okhttp)
    implementation(commonLib.bundles.ireader)

    testImplementation(test.junit4)
    testImplementation(test.junitAndroidExt)
    testImplementation(test.truth)
    testImplementation(test.coroutines)
    testImplementation(composeLib.compose.j4Unit)

    androidTestImplementation(test.junit4)
    androidTestImplementation(test.junitAndroidExt)
    androidTestImplementation(test.truth)
    androidTestImplementation(test.coroutines)
    androidTestImplementation(test.coroutines)
    androidTestImplementation(composeLib.compose.j4Unit)
    androidTestImplementation(commonLib.hilt.androidtest)

    // Instrumented Unit Tests
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("org.mockito:mockito-core:2.21.0")

    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
}
