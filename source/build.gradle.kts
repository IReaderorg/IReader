plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-kapt")
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


    implementation(project(Modules.core))
    addBaseDependencies()
    implementation(Moshi.moshi)
    implementation(Jsoup.jsoup)
    implementation(Ktor.core)
    implementation(Ktor.ktor_jsoup)
    implementation(Ktor.okhttp)
}

fun DependencyHandler.addBaseDependencies() {
    implementation(Timber.timber)
    implementation(Ktor.core)
    implementation(Ktor.serialization)
    implementation(Ktor.okhttp)
    implementation(Ktor.ktor_jsoup)
}
