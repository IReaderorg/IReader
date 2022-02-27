plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
    id("module-plugin")
}

addKtor()
addTesting()
dependencies {
    implementation(project(Modules.core))
    compileOnly(Deps.tachiyomi.api)
    // implementation(Deps.Ktor.ktor_jsoup)
    implementation(Deps.Moshi.moshi)
    // implementation(Deps.Jsoup.jsoup)
    implementation(Deps.kotlin.jsonSerialization)
    implementation(Deps.Timber.timber)

}
