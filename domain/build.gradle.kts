plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrainCompose)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
}

android {
    namespace = "ireader.domain"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
    }
    lint {
        targetSdk = ProjectConfig.targetSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${layout.buildDirectory.get().asFile.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}
kotlin {
    androidTarget {
        compilations {
            all {
                kotlinOptions.jvmTarget = ProjectConfig.androidJvmTarget.toString()
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                kotlinOptions.jvmTarget = ProjectConfig.desktopJvmTarget.toString()
            }
        }
    }


    sourceSets {
         commonMain {
            dependencies {
                implementation(project(Modules.commonResources))
                implementation(project(Modules.coreApi))
                implementation(project(Modules.sourceApi))
                api(compose.ui)
                api(compose.runtime)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.material3)
                api(kotlinx.coroutines.core)
                implementation(libs.ktor.core)
                implementation(libs.ktor.contentNegotiation)
                implementation(libs.ktor.okhttp)
                implementation(libs.bundles.ireader)
                implementation(kotlinx.serialization.protobuf)
                implementation(kotlinx.datetime)
                implementation(libs.okio)
                implementation(libs.jsoup)
                api(libs.koin.core)

                api(libs.coil.core)
            }
        }
         androidMain {
            dependencies {
                implementation("org.slf4j:slf4j-android:1.7.25")
                implementation(libs.bundles.simplestorage)
                implementation(androidx.biometric)
                implementation(androidx.lifecycle.viewmodelktx)
                implementation(androidx.lifecycle.viewmodelktx)
                implementation(composeLib.compose.googlFonts)
                implementation(androidx.media)

//                 Google ML Kit only for non-fdroid builds
//                 For fdroid, translation features will be disabled
                 implementation(libs.googleTranslator)

                implementation(libs.gson)
                implementation(androidx.work.runtime)
                /** Coroutine **/

                implementation(libs.okhttp.okhttp3)
                implementation(libs.okhttp.interceptor)
                implementation(libs.okhttp.doh)
                implementation(libs.okhttp.doh)

                implementation(androidx.dataStore)
                implementation(androidx.core)
                implementation(androidx.appCompat)
                implementation(androidx.webkit)
                implementation(androidx.browser)
                implementation(kotlinx.serialization.json)
                implementation(kotlinx.reflect)
                implementation(libs.coil.core)
                implementation(libs.coil.gif)
                /** LifeCycle **/
                implementation(androidx.lifecycle.runtime)
                implementation(kotlinx.stdlib)
                api(libs.koin.android)
                api(libs.koin.workManager)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}


dependencies {
    // Pure Kotlin EPUB implementation using ZipFile + Jsoup
    // No external EPUB libraries needed - works on both Android and Desktop
    // Uses existing dependencies: okio and jsoup
}