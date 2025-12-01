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

    sourceSets {
         commonMain {
            dependencies {
                implementation(project(Modules.domain))
                implementation(project(Modules.coreApi))
                implementation(project(Modules.sourceApi))
                implementation(project(Modules.data))
                implementation(project(Modules.commonResources))
                implementation(project(Modules.presentationCore))

                api(compose.foundation)
                api(compose.runtime)
                api(compose.animation)
                api(compose.animationGraphics)
                api(compose.materialIconsExtended)
                api(compose.preview)
                api(compose.ui)
                api(compose.components.resources)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material3)
                // Removed duplicate compose.materialIconsExtended


                implementation(libs.navigation.compose)
                // Voyager tab navigator removed - using pure Compose Navigation
                api(libs.koin.core)
                api(libs.koin.compose)
                
                // Immutable Collections - Critical for Compose performance (Mihon pattern)
                api(libs.kotlinx.collections.immutable)

                api(libs.coil.core)
                api(libs.coil.compose)
                api(libs.coil.network.ktor)

                implementation(libs.zxing.core)


            }
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
            }
        }
        androidMain {
            dependencies {
                api(libs.koin.android)
                api(androidx.biometric)
                api(libs.bundles.simplestorage)
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



            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                api(compose.desktop.currentOs)
                api(libs.kotlinx.coroutines.swing)

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



