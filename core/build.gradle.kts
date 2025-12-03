

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    alias(libs.plugins.jetbrainCompose)
    alias(kotlinx.plugins.compose.compiler)
}
android {
    namespace = "ireader.core"
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
    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
                }
            }
        }
    }
    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.desktopJvmTarget.toString()))
                }
            }
        }
    }
    
    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "core"
            isStatic = true
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
                api(libs.ktor.core)
                api(libs.ktor.contentNegotiation)
                // Needed for Compose Resources StringResource type
                implementation(compose.runtime)
                implementation(compose.components.resources)
                api(libs.ktor.contentNegotiation.kotlinx)
                api(libs.okio)
                api(libs.navigation.compose)
                api(libs.koin.core)
                api(libs.androidx.datastore.core)
                api(libs.androidx.datastore.preferences.core)
                
                // Immutable Collections - Critical for Compose performance (Mihon pattern)
                api(libs.kotlinx.collections.immutable)
                
                // Kermit logging - available transitively from source-api
                // Explicitly included here for direct usage in core module
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
                // Ktor mock engine for HTTP testing (matching project Ktor version 3.3.2)
                implementation(libs.ktor.client.mock)
            }
        }
         androidMain {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                // JVM-only Ktor serialization
                implementation(libs.ktor.contentNegotiation.gson)
                
                implementation(androidx.core)
                implementation(androidx.dataStore)
//                implementation(libs.quickjs.android)
                api(libs.ktor.okhttp)
            }
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.mock)
            }
        }
        
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                // JVM-only Ktor serialization
                implementation(libs.ktor.contentNegotiation.gson)
                
//                implementation(libs.quickjs.jvm)
                api(libs.ktor.okhttp)
            }
        }
        
        val desktopTest by getting {
            dependencies {
                implementation(libs.mock)
            }
        }
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.core)
                implementation("io.ktor:ktor-client-darwin:3.3.2")
            }
        }
        
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest.get())
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}
