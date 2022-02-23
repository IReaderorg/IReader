plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("module-plugin")
}

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Deps.Compose.composeVersion
    }
}

dependencies {
    addBaseDependencies()
    implementation(project(Modules.core))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.source))
    implementation(project(Modules.extensions))
    implementation(Deps.Compose.ui)
    implementation(Deps.Compose.runtime)
    implementation(Deps.Compose.navigation)
    implementation(Deps.Compose.material)
    debugImplementation(Deps.Compose.ui_test_manifest)
    implementation(Deps.Worker.runtimeKtx)
    implementation(Deps.DaggerHilt.worker)
    implementation(Deps.Coil.coilCompose)

    /** Room **/
    implementation(Deps.Room.roomRuntime)
    "kapt"(Deps.Room.roomCompiler)
    implementation(Deps.Room.roomKtx)
    implementation(Deps.Room.roomPaging)

    /** Coroutine **/
    implementation(Deps.Coroutines.core)
    implementation(Deps.Coroutines.android)

    /** Retrofit **/
    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.Retrofit.moshiConverter)

    implementation(Deps.OkHttp.okHttp3)

    implementation(Deps.OkHttp.okHttp3Interceptor)
    implementation(Deps.OkHttp.okhttp3_doh)
    implementation(Deps.OkHttp.okio)
    implementation(Deps.Compose.paging)
    implementation(Deps.Jsoup.jsoup)
    implementation(Deps.Jsonpathkt.jsonpathkt)
    implementation(Deps.Datastore.datastore)


}

fun Project.addKotlinCompilerFlags() {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs += listOf(
                "-XXLanguage:+InlineClasses",
                "-Xallow-result-return-type",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xuse-experimental=kotlin.ExperimentalStdlibApi"
            )
        }
    }
}


fun DependencyHandler.addBaseDependencies() {
    implementation(Deps.AndroidX.coreKtx)
    implementation(Deps.AndroidX.appCompat)
    implementation(Deps.AndroidX.webkit)
    implementation(Deps.AndroidX.browser)
    implementation(Deps.AndroidX.material)
    implementation(Deps.AndroidX.activity)

    implementation(Deps.Kotlin.jsonSerialization)


    kapt(Deps.DaggerHilt.hiltCompiler)
    implementation(Deps.DaggerHilt.hiltAndroid)
    implementation(Deps.DaggerHilt.hiltAndroidCompiler)
    implementation(Deps.Timber.timber)

    /** LifeCycle **/
    implementation(Deps.LifeCycle.runtimeKtx)
    implementation(Deps.LifeCycle.viewModel)


    testImplementation(Deps.Testing.junit4)
    testImplementation(Deps.Testing.junitAndroidExt)
    testImplementation(Deps.Testing.truth)
    testImplementation(Deps.Testing.coroutines)
    testImplementation(Deps.Testing.composeUiTest)


    androidTestImplementation(Deps.Testing.junit4)
    androidTestImplementation(Deps.Testing.junitAndroidExt)
    androidTestImplementation(Deps.Testing.truth)
    androidTestImplementation(Deps.Testing.coroutines)
    androidTestImplementation(Deps.Testing.composeUiTest)
    androidTestImplementation(Deps.Testing.hiltTesting)
    // Instrumented Unit Tests
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("org.mockito:mockito-core:2.21.0")
    implementation(kotlin("stdlib"))
    implementation(Deps.Ktor.core)
    implementation(Deps.Ktor.serialization)
    implementation(Deps.Ktor.okhttp)
    implementation(Deps.Ktor.ktor_jsoup)
}
