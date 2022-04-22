plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
}

android {
    compileSdk = ProjectConfig.compileSdk



    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
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

    implementation(project(Modules.coreApi))
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
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)


    implementation(libs.okhttp.interceptor)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)


    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
}
