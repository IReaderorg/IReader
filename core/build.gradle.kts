

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}
android {
    namespace = "ireader.core"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    buildFeatures {
        buildConfig = true
    }
}
kotlin {
    androidTarget {
        publishLibraryVariants("release")
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
                api(project(Modules.commonResources))
                api(project(Modules.sourceApi))
                api(kotlinx.coroutines.core)
                api(kotlinx.stdlib)
                api(kotlinx.datetime)
                api(kotlinx.serialization.json)
                implementation(libs.ktor.contentNegotiation.gson)
                api(libs.ktor.core)
                api(libs.ktor.contentNegotiation)
                api(libs.ktor.contentNegotiation.kotlinx)
                api(libs.okio)
                compileOnly(libs.jsoup)
                api(libs.koin.core)
                api(libs.multiplatformSettings)
                api(libs.multiplatformSettings.coroutines)
                api(libs.multiplatformSettings.serialization)
            }
        }
         androidMain {
            dependencies {
                implementation(androidx.core)
                implementation(androidx.dataStore)
                implementation(libs.quickjs.android)
                api(libs.ktor.okhttp)
                compileOnly(libs.jsoup)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                implementation(libs.quickjs.jvm)
                api(libs.ktor.okhttp)
                compileOnly(libs.jsoup)
            }
        }
    }
}
