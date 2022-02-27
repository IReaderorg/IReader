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
    lint {
        baseline = file("lint-baseline.xml")
    }
}

addCompose()
addAccompanist()

dependencies {
    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.coreUi))

    compileOnly(Deps.tachiyomi.api)
    compileOnly(Deps.tachiyomi.core)

    implementation(Deps.androidx.core)
    implementation(Deps.androidx.appCompat)
    implementation(Deps.jsoup)
    implementation(Deps.Worker.runtimeKtx)
    implementation(Deps.Room.roomRuntime)
    kapt(Deps.Room.roomCompiler)
    implementation(Deps.Room.roomKtx)
    implementation(Deps.Room.roomPaging)
    implementation(Deps.Moshi.moshi)
    implementation(Deps.Moshi.moshiKotlin)


    implementation(Deps.DaggerHilt.worker)
    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.ktor.okhttp)
    implementation(Deps.Compose.paging)

    implementation(Deps.Compose.hiltNavigationCompose)
    implementation(Deps.DaggerHilt.hiltAndroid)
    implementation(Deps.DaggerHilt.hiltAndroidCompiler)
    implementation(Deps.DaggerHilt.hiltCompiler)


}