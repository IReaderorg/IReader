plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(Modules.coreApi))
    implementation(project(Modules.commonModels))
    implementation(project(Modules.uiImageLoader))

    implementation(androidx.appCompat)

    implementation(androidx.core)
    implementation(kotlinx.coroutines.android)
    implementation(androidx.lifecycle.viewModel)
    implementation(androidx.lifecycle.viewmodelktx)
    implementation(androidx.lifecycle.runtime)

    implementation(compose.compose.ui)
    implementation(compose.compose.coil)
    implementation(androidx.browser)

    implementation(libs.okhttp.doh)
    implementation(libs.okio)

    implementation(libs.hilt.android)

    implementation(libs.jsoup)
    implementation(androidx.dataStore)

    implementation(kotlinx.stdlib)

    implementation(libs.ktor.core)
    implementation(libs.ktor.core.android)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.okhttp)

    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
}
