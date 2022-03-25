plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
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

    implementation(project(Modules.core))
    implementation(project(Modules.coreUi))


    // implementation(Deps.tachiyomi.api)
    implementation(libs.tachiyomi)


    implementation(kotlinx.datetime)
    implementation(compose.compose.activity)
    implementation(compose.compose.ui)
    implementation(compose.compose.runtime)
    implementation(compose.compose.navigation)
    implementation(compose.compose.material)
    implementation(compose.compose.coil)
    implementation(compose.compose.paging)
    debugImplementation(compose.compose.uiTestManifest)

    implementation(androidx.work.runtime)
    implementation(libs.hilt.worker)

    /** Room **/
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    kapt(libs.room.compiler)


    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)


    /** Retrofit **/
    implementation(libs.retrofit.retrofit)
    implementation(libs.retrofit.moshiConverter)

    implementation(libs.okhttp.okhttp3)
    implementation(libs.okhttp.interceptor)
    implementation(libs.okhttp.doh)


    implementation(libs.okhttp.doh)


    implementation(libs.okio)
    implementation(libs.jsoup)

    implementation(androidx.dataStore)

    implementation(androidx.core)
    implementation(androidx.appCompat)
    implementation(androidx.webkit)
    implementation(androidx.browser)
    implementation(androidx.material)
    implementation(kotlinx.serialization.json)
    implementation(kotlinx.reflect)



    kapt(libs.hilt.compiler)
    implementation(libs.hilt.androidcompiler)
    implementation(libs.hilt.android)
    implementation(libs.timber)

    /** LifeCycle **/
    implementation(kotlinx.lifecycle.viewModel)
    implementation(androidx.lifecycle.runtime)

    implementation(kotlinx.stdlib)
    implementation(libs.ktor.core)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.okhttp)
    implementation(libs.ktor.jsoup)


    testImplementation(test.junit4)
    testImplementation(test.junitAndroidExt)
    testImplementation(test.truth)
    testImplementation(test.coroutines)
    testImplementation(compose.compose.uiTesting)

    androidTestImplementation(test.junit4)
    androidTestImplementation(test.junitAndroidExt)
    androidTestImplementation(test.truth)
    androidTestImplementation(test.coroutines)
    androidTestImplementation(test.coroutines)
    androidTestImplementation(compose.compose.uiTesting)
    androidTestImplementation(libs.hilt.androidtest)

    // Instrumented Unit Tests
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("org.mockito:mockito-core:2.21.0")


}