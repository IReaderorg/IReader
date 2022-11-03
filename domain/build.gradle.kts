plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ireader.domain"
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}

dependencies {

    implementation("io.github.kazemcodes:epublib-core:4.0-SNAPSHOT") {
        exclude(group = "org.slf4j")
        exclude(group = "xmlpull")
    }
    implementation("org.slf4j:slf4j-android:1.7.25")


    //implementation(project(Modules.coreUi))

    implementation(project(Modules.commonResources))




    implementation(project(Modules.coreApi))
    implementation(libs.bundles.simplestorage)


    implementation(androidx.biometric)
    implementation(androidx.lifecycle.viewmodelktx)
    implementation(composeLib.compose.googlFonts)
    implementation(composeLib.material3.core)

    implementation(androidx.media)
    implementation(kotlinx.serialization.protobuf)
    implementation(kotlinx.datetime)
    implementation(composeLib.compose.runtime)

    implementation(libs.googleTranslator)

    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.paging)
    implementation(libs.gson)

    debugImplementation(composeLib.compose.uiTestManifest)

    implementation(androidx.work.runtime)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)

    implementation(libs.okhttp.okhttp3)
    implementation(libs.okhttp.interceptor)
    implementation(libs.okhttp.doh)

    implementation(libs.okhttp.doh)

    implementation(libs.okio)
    implementation(libs.jsoup)

    implementation(androidx.dataStore)

    implementation(androidx.core)
    implementation(androidx.appCompat)
    implementation(androidx.webkit)
    implementation(androidx.browser)
    implementation(androidx.material)
    implementation(kotlinx.serialization.json)
    implementation(kotlinx.reflect)


    implementation(libs.coil.core)
    implementation(libs.coil.gif)

    /** LifeCycle **/
    implementation(androidx.lifecycle.runtime)

    implementation(kotlinx.stdlib)
    implementation(libs.ktor.core)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.okhttp)
    implementation(libs.bundles.ireader)


    implementation(libs.koin.android)
    ksp(libs.koin.kspCompiler)
    implementation(libs.koin.androidCompose)
    compileOnly(libs.koin.annotations)
    compileOnly(libs.koin.workManager)
}
