plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
}

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compose.get()
    }
}

dependencies {

    implementation("nl.siegmann.epublib:epublib-core:3.1") {
        exclude(group="org.slf4j")
        exclude(group="xmlpull")
    }
    implementation("org.slf4j:slf4j-android:1.7.25")


    implementation(project(Modules.core))
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
    implementation(compose.compose.activity)
    implementation(compose.compose.ui)
    implementation(compose.compose.runtime)
    implementation(compose.compose.navigation)
implementation(compose.compose.material3)


    implementation(compose.compose.coil)
    implementation(compose.compose.paging)
    implementation(common.gson)
    implementation(project(mapOf("path" to ":common-models")))
    debugImplementation(compose.compose.uiTestManifest)

    implementation(androidx.work.runtime)
    implementation(common.hilt.worker)

    /** Room **/
    implementation(common.room.runtime)
    implementation(common.room.ktx)
    // implementation(common.room.paging)
    kapt(common.room.compiler)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)

    implementation(common.okhttp.okhttp3)
    implementation(common.okhttp.interceptor)
    implementation(common.okhttp.doh)

    implementation(common.okhttp.doh)

    implementation(common.okio)
    implementation(common.jsoup)

    implementation(androidx.dataStore)

    implementation(androidx.core)
    implementation(androidx.appCompat)
    implementation(androidx.webkit)
    implementation(androidx.browser)
    implementation(androidx.material)
    implementation(kotlinx.serialization.json)
    implementation(kotlinx.reflect)

    kapt(common.hilt.androidcompiler)
    kapt(common.hilt.compiler)
    implementation(common.hilt.android)

    /** LifeCycle **/
    //  implementation(kotlinx.lifecycle.viewModel)
    implementation(androidx.lifecycle.runtime)

    implementation(kotlinx.stdlib)
    implementation(common.ktor.core)
    implementation(common.ktor.contentNegotiation)
    implementation(common.ktor.okhttp)
    implementation(common.bundles.ireader)


    testImplementation(test.junit4)
    testImplementation(test.junitAndroidExt)
    testImplementation(test.truth)
    testImplementation(test.coroutines)
    testImplementation(compose.compose.j4Unit)

    androidTestImplementation(test.junit4)
    androidTestImplementation(test.junitAndroidExt)
    androidTestImplementation(test.truth)
    androidTestImplementation(test.coroutines)
    androidTestImplementation(test.coroutines)
    androidTestImplementation(compose.compose.j4Unit)
    androidTestImplementation(common.hilt.androidtest)

    // Instrumented Unit Tests
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("org.mockito:mockito-core:2.21.0")

    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
}
