plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrainCompose)
    id("kotlinx-serialization")
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
                implementation(project(Modules.commonResources))

                // Compose
                api(compose.foundation)
                api(compose.runtime)
                api(compose.animation)
                api(compose.animationGraphics)
                api(compose.materialIconsExtended)
                api(compose.preview)
                api(compose.ui)
                api(compose.components.resources)
                api(compose.material3)

                // Navigation
                implementation(libs.navigation.compose)
                
                // Dependency Injection
                api(libs.koin.core)
                api(libs.koin.compose)

                // Image Loading
                api(libs.coil.core)
                api(libs.coil.compose)
                api(libs.coil.network.ktor)

                // Logging
                implementation(libs.napier)
            }
        }
        androidMain {
            dependencies {
                api(libs.koin.android)
                api(composeLib.material3.windowsizeclass)
                api(composeLib.compose.activity)
                api(composeLib.compose.ui.util)
                api(androidx.activity)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                api(compose.desktop.currentOs)
                api(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

android {
    namespace = "ireader.presentation.core"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
    }
    

    
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    lint {
        targetSdk = ProjectConfig.targetSdk
    }
}



