@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrainCompose)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
                    }
                }
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.desktopJvmTarget.toString()))
                    }
                }
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "presentation"
            isStatic = true
            
            // Note: We don't export data module to avoid SQLDelight cinterop issues
            // The Main.kt entry point is self-contained for now
        }
    }

    sourceSets {
         commonMain {
            dependencies {
                api(project(Modules.domain))
                api(project(Modules.coreApi))
                api(project(Modules.sourceApi))
                api(project(Modules.data))
                api(project(Modules.commonResources))
                api(project(Modules.presentationCore))

                api(compose.foundation)
                api(compose.runtime)
                api(compose.animation)
                api(compose.animationGraphics)
                api(compose.materialIconsExtended)
                api(compose.ui)
                api(compose.components.resources)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material3)

                implementation(libs.navigation.compose)
                api(libs.koin.core)
                api(libs.koin.compose)
                
                // Immutable Collections - Critical for Compose performance (Mihon pattern)
                api(libs.kotlinx.collections.immutable)

                api(libs.coil.core)
                api(libs.coil.compose)
                api(libs.coil.network.ktor)

                implementation(libs.zxing.core)
                
                // FileKit - Modern KMP file picker
                api(libs.bundles.filekit)
            }
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        androidMain {
            dependencies {
                api(libs.koin.android)
                api(androidx.biometric)
                api(libs.core.splashscreen)
                api(composeLib.compose.googlFonts)
                api(composeLib.compose.activity)
                api(libs.ktor.core.android)

                api(composeLib.material3.windowsizeclass)

                api(composeLib.compose.ui.util)
                api(composeLib.compose.constraintlayout)
                api(accompanist.permissions)
                api(androidx.appCompat)
                api(androidx.activity)
                api(androidx.webkit)
                api(androidx.media)
                api(androidx.emoji)
                api(androidx.work.runtime)
                
                // Preview only available on Android/Desktop
                api(compose.preview)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                api(compose.desktop.currentOs)
                api(libs.kotlinx.coroutines.swing)
                // Preview only available on Android/Desktop
                api(compose.preview)

                val lwjglVersion = "3.3.1"
                listOf("lwjgl", "lwjgl-nfd").forEach { lwjglDep ->
                    implementation("org.lwjgl:${lwjglDep}:${lwjglVersion}")
                    listOf(
                        "natives-windows", "natives-windows-x86", "natives-windows-arm64",
                        "natives-macos", "natives-macos-arm64",
                        "natives-linux", "natives-linux-arm64", "natives-linux-arm32"
                    ).forEach { native ->
                        runtimeOnly("org.lwjgl:${lwjglDep}:${lwjglVersion}:${native}")
                    }
                }
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
                // iOS-specific dependencies if needed
            }
        }
    }
}

android {
    namespace = "ireader.presentation"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
    }
    

    
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    lint {
        baseline = file("lint-baseline.xml")
        targetSdk = ProjectConfig.targetSdk
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${layout.buildDirectory.get().asFile.absolutePath}/generated/ksp/${name}/kotlin")

        }
    }
}



