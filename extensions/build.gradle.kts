plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
}

android {
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
dependencies {
    implementation(project(Modules.source))
    implementation(Moshi.moshi)
    implementation(Jsoup.jsoup)
    implementation(Kotlin.jsonSerialization)
    implementation(Timber.timber)
    testImplementation(Testing.junit4)
    testImplementation(Testing.junitAndroidExt)
    testImplementation(Testing.truth)
    testImplementation(Testing.coroutines)
    androidTestImplementation(Testing.junit4)
    androidTestImplementation(Testing.junitAndroidExt)
    androidTestImplementation(Testing.truth)
    androidTestImplementation(Testing.coroutines)
    implementation(Ktor.core)
    implementation(Ktor.serialization)
    implementation(Ktor.okhttp)
    implementation(Ktor.ktor_jsoup)
}
