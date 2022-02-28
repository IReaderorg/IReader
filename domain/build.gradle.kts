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


    implementation(Deps.tachiyomi.api)
    compileOnly(Deps.tachiyomi.core)
    compileOnly(Deps.tachiyomi.core_jvm)
    //implementation(Deps.tachiyomi.core)


    implementation(Deps.Compose.ui)
    implementation(Deps.kotlin.datetime)
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

    implementation(Deps.okhttp.okHttp3)

    implementation(Deps.okhttp.okHttp3Interceptor)
    implementation(Deps.okhttp.okhttp3_doh)
    implementation(Deps.okio)
    implementation(Deps.Compose.paging)
    implementation(Deps.jsoup)
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
    implementation(Deps.androidx.core)
    implementation(Deps.androidx.appCompat)
    implementation(Deps.androidx.webkit)
    implementation(Deps.androidx.browser)
    implementation(Deps.androidx.material)
    implementation(Deps.androidx.compose.activity)

    implementation(Deps.kotlin.jsonSerialization)
    implementation(Deps.kotlin.reflect)


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
    implementation(Deps.kotlin.stdlib)
    implementation(Deps.ktor.core)
    implementation(Deps.ktor.serialization)
    implementation(Deps.ktor.okhttp)
    implementation(Deps.ktor.ktor_jsoup)
}
