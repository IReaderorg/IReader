plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("kotlin-kapt")
}

dependencies {
    implementation(Deps.androidx.appCompat)

    //compileOnly(Deps.tachiyomi.api)
    implementation(Deps.tachiyomi.core)

    implementation(Deps.Compose.ui)
    implementation(Deps.Coil.coilCompose)

    implementation(Deps.okhttp.okhttp3_doh)
    implementation(Deps.okio)

    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.Retrofit.moshiConverter)

    implementation(Deps.Moshi.moshi)
    implementation(Deps.Moshi.moshiKotlin)

    implementation(Deps.jsoup)
    implementation(Deps.Datastore.datastore)

    implementation(Deps.DaggerHilt.hiltAndroid)

    implementation(kotlin("stdlib"))

    implementation(Deps.ktor.core)
    implementation(Deps.ktor.serialization)
    implementation(Deps.ktor.okhttp)
    implementation(Deps.ktor.ktor_jsoup)

    implementation(Deps.Timber.timber)
}