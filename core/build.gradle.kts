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

    implementation(common.okhttp.doh)
    implementation(common.okio)

    implementation(common.hilt.android)

    implementation(common.jsoup)
    implementation(androidx.dataStore)

    implementation(kotlinx.stdlib)

    implementation(common.ktor.core)
    implementation(common.ktor.core.android)
    implementation(common.ktor.contentNegotiation)
    implementation(common.ktor.okhttp)

    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
}
