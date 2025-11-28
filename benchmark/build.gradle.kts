plugins {
    id("com.android.test")
    kotlin("android")
    id("androidx.baselineprofile")
}

android {
    namespace = "org.ireader.benchmark"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        minSdk = 28 // Macrobenchmark requires API 28+
        targetSdk = ProjectConfig.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Required for benchmark tests
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
        }
    }

    // Must match app's build types
    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
        create("release") {
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
    
    // Must match app's flavor dimensions
    flavorDimensions += "default"
    
    productFlavors {
        create("standard") {
            dimension = "default"
        }
        create("fdroid") {
            dimension = "default"
        }
        create("dev") {
            dimension = "default"
        }
    }

    targetProjectPath = ":android"
    
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.uiautomator)
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark" || it.buildType == "release"
    }
}
