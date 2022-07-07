plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "org.ireader.data"
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
    implementation(project(Modules.commonResources))

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


    kapt(commonLib.hilt.androidcompiler)
    implementation(commonLib.hilt.android)



    implementation(commonLib.jsoup)

    /** LifeCycle **/
    implementation(androidx.lifecycle.runtime)
    implementation(androidx.lifecycle.viewModel)

    implementation(kotlinx.stdlib)

    implementation(commonLib.room.runtime)
    implementation(commonLib.room.ktx)
    kapt(commonLib.room.compiler)
    implementation(commonLib.room.roomigrant.lib)
    kapt(commonLib.room.roomigrant.compiler)

    implementation(commonLib.okhttp.interceptor)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)

    testImplementation(test.bundles.common)
    testImplementation(commonLib.hilt.androidtest)
    testImplementation(commonLib.room.testing)
    androidTestImplementation(commonLib.hilt.androidtest)
    androidTestImplementation(commonLib.room.testing)
    androidTestImplementation(test.bundles.common)
}
