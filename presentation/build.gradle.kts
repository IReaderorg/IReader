plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-kapt")
    id("module-plugin")
}

android {

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Deps.Compose.composeVersion
    }
}

addCompose()
addAccompanist()

dependencies {
    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.source))
    implementation(Deps.AndroidX.coreKtx)
    implementation(Deps.AndroidX.appCompat)
    implementation(Deps.Jsoup.jsoup)
    implementation(Deps.Worker.runtimeKtx)
    implementation(Deps.Room.roomRuntime)
    kapt(Deps.Room.roomCompiler)
    implementation(Deps.Room.roomKtx)
    implementation(Deps.Room.roomPaging)
    implementation(Deps.Moshi.moshi)
    implementation(Deps.Moshi.moshiKotlin)


    implementation(Deps.DaggerHilt.worker)
    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.Ktor.okhttp)
    implementation(Deps.Compose.paging)

    implementation(Deps.Compose.hiltNavigationCompose)
    implementation(Deps.DaggerHilt.hiltAndroid)
    implementation(Deps.DaggerHilt.hiltAndroidCompiler)
    implementation(Deps.DaggerHilt.hiltCompiler)


}