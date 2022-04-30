plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp").version("1.6.20-1.0.5")
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
        resources.excludes.addAll(
            listOf(
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
            )
        )
    }
}

dependencies {

    implementation(project(Modules.coreApi))
    implementation(project(Modules.core))
    implementation(project(Modules.domain))
    implementation(project(Modules.commonModels))
    implementation(project(Modules.commonData))
    implementation(project(Modules.coreCatalogs))
    implementation(project(Modules.commonExtensions))

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
    implementation(project(mapOf("path" to ":core-ui")))

    //ksp(libs.moshi.codegen)

    kapt(libs.hilt.compiler)
    implementation(libs.hilt.androidcompiler)
    implementation(libs.hilt.android)

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
    testImplementation(libs.hilt.androidtest)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.hilt.androidtest)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(test.bundles.common)
}
