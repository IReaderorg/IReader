plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("module-plugin")
}

android {
    compileSdk = ProjectConfig.compileSdk


    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

addTesting()
addKtor()

dependencies {
    //implementation(project(Modules.source))

    compileOnly(Deps.tachiyomi.core)
    compileOnly(Deps.tachiyomi.api)
    implementation(project(Modules.core))
    implementation(project(Modules.domain))
    implementation(project(Modules.extensions))

    implementation(Deps.androidx.core)
    implementation(Deps.androidx.appCompat)
    implementation(Deps.androidx.webkit)
    implementation(Deps.androidx.browser)
    implementation(Deps.androidx.material)
    implementation(Deps.androidx.compose.activity)

    implementation(Deps.kotlin.jsonSerialization)


    kapt(Deps.DaggerHilt.hiltCompiler)
    implementation(Deps.DaggerHilt.hiltAndroid)
    implementation(Deps.DaggerHilt.hiltAndroidCompiler)
    implementation(Deps.Timber.timber)

    /** LifeCycle **/
    implementation(Deps.LifeCycle.runtimeKtx)
    implementation(Deps.LifeCycle.viewModel)

    implementation(kotlin("stdlib"))


    implementation(Deps.Room.roomRuntime)
    implementation(Deps.Compose.runtime)
    kapt(Deps.Room.roomCompiler)
    implementation(Deps.Room.roomKtx)
    implementation(Deps.Room.roomPaging)
    androidTestImplementation(Deps.Room.roomTesting)
    implementation(Deps.Moshi.moshi)
    implementation(Deps.Moshi.moshiKotlin)
    implementation(Deps.jsoup)
    implementation(Deps.okhttp.okHttp3Interceptor)

    /** Coroutine **/
    implementation(Deps.Coroutines.core)
    implementation(Deps.Coroutines.android)

    /** Retrofit **/
    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.Retrofit.moshiConverter)
    implementation(Deps.Retrofit.moshiConverter)
    testImplementation("androidx.test:monitor:1.6.0-alpha01")

}
