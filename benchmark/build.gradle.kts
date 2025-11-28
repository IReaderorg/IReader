plugins {
    id("com.android.test")
    kotlin("android")
}

android {
    namespace = "org.ireader.benchmark"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        minSdk = 28 // Macrobenchmark requires API 28+
        targetSdk = ProjectConfig.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Suppress benchmark errors for debug builds and emulators
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "DEBUGGABLE,EMULATOR,LOW-BATTERY,UNLOCKED"
        
        // Auto-grant permissions to avoid dialogs during benchmarks
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
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

    buildTypes {
        // Use debug build for benchmarking (signed, can be installed)
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            // Fall back to debug build type which is signed
            matchingFallbacks += listOf("debug")
        }
    }
    
    // Match app's flavor dimensions
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

dependencies {
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.uiautomator)
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}

// Workaround for Gradle file locking issues on Windows
tasks.withType<Test>().configureEach {
    doNotTrackState("Benchmark tests have file locking issues")
}
