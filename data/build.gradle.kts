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
    implementation(project(Modules.source))
    implementation(project(Modules.core))
    implementation(project(Modules.domain))

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

    implementation(kotlin("stdlib"))


    implementation(Deps.Room.roomRuntime)
    implementation(Deps.Compose.runtime)
    kapt(Deps.Room.roomCompiler)
    implementation(Deps.Room.roomKtx)
    implementation(Deps.Room.roomPaging)
    androidTestImplementation(Deps.Room.roomTesting)
    implementation(Deps.Moshi.moshi)
    implementation(Deps.Moshi.moshiKotlin)
    implementation(Deps.Jsoup.jsoup)
    implementation(Deps.OkHttp.okHttp3Interceptor)

    /** Coroutine **/
    implementation(Deps.Coroutines.core)
    implementation(Deps.Coroutines.android)

    /** Retrofit **/
    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.Retrofit.moshiConverter)
    implementation(Deps.Retrofit.moshiConverter)
    testImplementation("androidx.test:monitor:1.6.0-alpha01")

}
