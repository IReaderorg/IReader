plugins {
    id("com.android.library")
    id("module-plugin")
}

dependencies {
    implementation(Deps.AndroidX.appCompat)

    implementation(Deps.Compose.ui)
    implementation(Deps.Coil.coilCompose)

    implementation(Deps.OkHttp.okhttp3_doh)
    implementation(Deps.OkHttp.okio)

    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.Retrofit.moshiConverter)

    implementation(Deps.Moshi.moshi)
    implementation(Deps.Moshi.moshiKotlin)

    implementation(Deps.Jsoup.jsoup)
    implementation(Deps.Datastore.datastore)

    implementation(Deps.DaggerHilt.hiltAndroid)

    implementation(kotlin("stdlib"))

    implementation(Deps.Ktor.core)
    implementation(Deps.Ktor.serialization)
    implementation(Deps.Ktor.okhttp)
    implementation(Deps.Ktor.ktor_jsoup)

    implementation(Deps.Timber.timber)
}
