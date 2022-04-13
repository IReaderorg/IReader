plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = ProjectConfig.compileSdk



    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
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
            "META-INF/*",
            "META-INF/gradle/incremental.annotation.processors"
        ))
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
    implementation(kotlinx.datetime)
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
    ksp(libs.room.compiler)
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

    androidTestImplementation(test.junit4)
    androidTestImplementation(test.junitAndroidExt)
    androidTestImplementation(test.truth)
    androidTestImplementation(test.extJunit)
    androidTestImplementation(test.testRunner)
    androidTestImplementation(test.espresso)

}
