plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
}

android {
    compileSdk = ProjectConfig.compileSdk


    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}


dependencies {
    implementation(libs.tachiyomi)
    implementation(project(Modules.core))
    implementation(project(Modules.domain))

    implementation(androidx.core)
    implementation(androidx.appCompat)
    implementation(androidx.webkit)
    implementation(androidx.browser)
    implementation(androidx.material)
    implementation(kotlinx.serialization.json)
    implementation(kotlinx.reflect)
    implementation(kotlinx.serialization.json)
    implementation(compose.compose.activity)

    kapt(libs.hilt.compiler)
    implementation(libs.hilt.androidcompiler)
    implementation(libs.hilt.android)

    implementation(libs.timber)
    implementation(libs.jsoup)

    /** LifeCycle **/
    implementation(androidx.lifecycle.runtime)
    implementation(androidx.lifecycle.viewModel)

    implementation(kotlinx.stdlib)


    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    androidTestImplementation(libs.room.testing)


    implementation(libs.moshi.moshi)
    implementation(libs.moshi.kotlin)

    implementation(libs.okhttp.interceptor)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)

    /** Retrofit **/
    implementation(libs.retrofit.retrofit)
    implementation(libs.retrofit.moshiConverter)
    testImplementation("androidx.test:monitor:1.6.0-alpha01")

}
