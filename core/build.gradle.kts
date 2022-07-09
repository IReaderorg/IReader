plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("kotlin-kapt")
}
android {
    namespace = "org.ireader.core"
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

    implementation(composeLib.compose.ui)
    implementation(composeLib.compose.coil)
    implementation(androidx.browser)

    implementation(commonLib.okhttp.doh)
    implementation(commonLib.okio)

    implementation(commonLib.hilt.android)

    implementation(commonLib.jsoup)
    implementation(androidx.dataStore)

    implementation(kotlinx.stdlib)

    implementation(commonLib.ktor.core)
    implementation(commonLib.ktor.core.android)
    implementation(commonLib.ktor.contentNegotiation)
    implementation(commonLib.ktor.okhttp)

    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
}
