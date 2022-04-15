plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(Modules.coreApi))

    implementation(androidx.appCompat)

    implementation(compose.compose.ui)
    implementation(compose.compose.coil)
    implementation(androidx.browser)

    implementation(libs.okhttp.doh)
    implementation(libs.okio)

    implementation(libs.retrofit.retrofit)
    implementation(libs.retrofit.moshiConverter)

    implementation(libs.hilt.android)
    implementation(libs.moshi.moshi)
    implementation(libs.moshi.kotlin)

    implementation(libs.jsoup)
    implementation(androidx.dataStore)


    implementation(kotlinx.stdlib)

    implementation(libs.ktor.core)
    implementation(libs.ktor.core.android)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.okhttp)

    implementation(libs.timber)
    implementation(test.junit4)
    implementation(test.extJunit)
    implementation(test.espresso)
    implementation(test.junitAndroidExt)
    implementation(test.truth)
}