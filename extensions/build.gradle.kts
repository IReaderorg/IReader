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
    implementation(project(Modules.source))
    implementation(Deps.Moshi.moshi)
    implementation(Deps.Jsoup.jsoup)
    implementation(Deps.Kotlin.jsonSerialization)
    implementation(Deps.Timber.timber)

}
