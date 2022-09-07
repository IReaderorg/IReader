plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ireader.domain"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.compiler.get()
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}

dependencies {

    implementation("nl.siegmann.epublib:epublib-core:3.1") {
        exclude(group = "org.slf4j")
        exclude(group = "xmlpull")
    }
    implementation("org.slf4j:slf4j-android:1.7.25")


    implementation(project(Modules.coreUi))
    implementation(project(Modules.commonData))
    implementation(project(Modules.commonResources))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.commonExtensions))
    implementation(project(Modules.coreCatalogs))

    implementation(project(Modules.coreApi))

    implementation(androidx.media)
    implementation(kotlinx.serialization.protobuf)
    implementation(kotlinx.datetime)
    implementation(composeLib.compose.activity)
    implementation(composeLib.compose.ui)
    implementation(composeLib.compose.runtime)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.material3.core)
    implementation(libs.googleTranslator)

    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.paging)
    implementation(libs.gson)
    implementation(project(mapOf("path" to ":common-models")))
    debugImplementation(composeLib.compose.uiTestManifest)

    implementation(androidx.work.runtime)


    /** Room **/
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    // implementation(libs.room.paging)
    kapt(libs.room.compiler)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)

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


    /** LifeCycle **/
    //  implementation(kotlinx.lifecycle.viewModel)
    implementation(androidx.lifecycle.runtime)

    implementation(kotlinx.stdlib)
    implementation(libs.ktor.core)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.okhttp)
    implementation(libs.bundles.ireader)

    testImplementation(test.junit4)
    testImplementation(test.junitAndroidExt)
    testImplementation(test.truth)
    testImplementation(test.coroutines)
    testImplementation(composeLib.compose.j4Unit)

    androidTestImplementation(test.junit4)
    androidTestImplementation(test.junitAndroidExt)
    androidTestImplementation(test.truth)
    androidTestImplementation(test.coroutines)
    androidTestImplementation(test.coroutines)
    androidTestImplementation(composeLib.compose.j4Unit)
    //androidTestImplementation(libs.hilt.androidtest)

    // Instrumented Unit Tests
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("org.mockito:mockito-core:2.21.0")

    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)


    implementation(libs.koin.android)
    ksp(libs.koin.kspCompiler)
    implementation(libs.koin.androidCompose)
    compileOnly(libs.koin.annotations)
    compileOnly(libs.koin.workManager)
}
